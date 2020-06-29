import time
from collections import deque
from itertools import islice
import math

from anytree import Node
import numpy as np
import gym
from gym import error, spaces, utils
from gym.utils import seeding

import config as cf
from obligation_space import ObligationSpace
from subepisode import Subepisode
from key_connector import KeYConnector
from feature_extractor import FeatureExtractor
from rewarder.rewarder_factory import create_rewarder
from datastructures.po_anytree import print_anytree
from datastructures.po_node import PONode
from datastructures.fixed_length_deque import FixedLengthDeque


class MilkeyEnv(gym.Env):
    metadata = {'render.modes': ['human']} # needed?

    def __init__(self):
        '''
        Initializes all env variables necessary for running the environment.
        Properly initializes the KeY Connector and sets up counters etc.
        '''
        self.connector = KeYConnector()
        self.extractor = FeatureExtractor()
        self.rewarder = create_rewarder(cf.REWARDER_TYPE, cf.REWARD_EPISODE_END, cf.PENALTY_EPISODE_END, cf.PENALTY_STEP)
        self.ac_space = spaces.Discrete(len(self.connector.available_tactics))
        self.ob_space = ObligationSpace(self.connector, self.extractor)

        self.max_tree_depth = cf.ROOT_EPIS_MAX_DEPTH
        self.max_steps_per_po = cf.MAX_STEPS_PER_PO
        self.pre_kill = cf.PRE_KILL_FAILED_EPISODES

        self.env_steps = 0
        self.po_steps = 0 # number of steps for the current po (the currently loaded file)
        self.po_closable = True # while proving a PO, set this to false as soon as anything fails. Reset for new PO.
        self.batched_exp = []

        self.po_success_history = FixedLengthDeque(cf.POWISE_SUCCESS_BUFFER_SIZE) # holds the results of the last POWISE_SUCCESS_BUFFER_SIZE po's.
        self.reward_history = FixedLengthDeque(cf.STEPWISE_REWARD_BUFFER_SIZE) # holds the rewards of the last STEPWISE_REWARD_BUFFER_SIZE steps.
        self.tactic_history = FixedLengthDeque(cf.STEPWISE_TACTIC_BUFFER_SIZE) # holds the tactics used in the last STEPWISE_TACTIC_BUFFER_SIZE steps.

        # if REPRINT_SUCCESSFUL_EPISODES is set to True, this list contains the proving history of all successfully closed POs.
        self.successful_po_proofs = []

        # each loaded file is a proof obligation (po). Set to -1 since there is an initial reset before learning starts.
        self.total_po_count = 0
        self.successful_po_count = 0

        # the goals initially open on po loading are called topgoals.
        self.total_topgoal_count = 0
        self.successful_topgoal_count = 0

        # a subepisode (subep) may split into several new goals, creating new subeps. 
        self.total_subep_count = 0
        self.successful_subep_count = 0

        self.open_subepisodes = deque()

        # call reset to prepare KeY for its job
        self.reset()

    def _del(self):
        '''
        To be run on deletion/program end: closes KeY.
        '''
        if self.connector:
            self.connector.quit_key()

# -------------------------------------------------------------------------------------------

    def step(self, action):
        '''
        Executes a learning step: maps given action to a tactic, executes it
        and observes what is happening. Deals with all the eventualities of the
        proving process.
        '''

        # time.sleep(0.2)
        self.env_steps += 1

        # pre-execution episode checks
        if self.cur_subepis.child_episodes: return self._episode_exit("parent", 'EXIT_PRE_EXECUTION')
        if self.po_steps > self.max_steps_per_po: return self._episode_exit("crash", 'EXIT_PRE_EXECUTION')
        if self.cur_subepis.max_steps_reached(): return self._episode_exit("fail", 'EXIT_PRE_EXECUTION')

        self.po_steps += 1

        # tactic preparation
        cur_tactic = self.connector.available_tactics[action]
        self.tactic_history.append('{0} on #{1}'.format(cur_tactic, self.cur_subepis.cur_goal.id))

        # tactic execution
        cur_goal_id = self.cur_subepis.cur_goal.id
        new_goal_ids = self.connector.execute_tactic(cur_goal_id, cur_tactic)
        self.cur_subepis.add_tactic_app_step(cur_tactic)

        # post-execution episode checks
        if not new_goal_ids: return self._episode_exit("success", 'EXIT_POST_EXECUTION')
        if new_goal_ids == [-1]: return self._episode_exit("crash", 'EXIT_POST_EXECUTION')

        # Create nodes from the resulting PO IDs
        new_goals = []
        for goal_id in new_goal_ids:
            new_goal_node = PONode.from_id(goal_id, self.connector, self.extractor, parent=self.cur_subepis.cur_goal)
            # to catch those rare cases of failed node creations
            if new_goal_node.id == -1: return self._episode_exit("crash", 'EXIT_POST_EXECUTION')
            new_goals.append(new_goal_node)
                 
        # Multiple new goals -> subepisode becomes parent and env continues with a subepisode
        if len(new_goals) > 1:
            child_episodes = [Subepisode(root_goal=new_goal, max_steps=self.cur_subepis.steps_remaining(), \
                    parent_episode=self.cur_subepis) for new_goal in new_goals]
            self.cur_subepis.become_parent(child_episodes)
            # append newly made parent to open subepisode stack, followed by its children
            self.open_subepisodes.append(self.cur_subepis)
            self.open_subepisodes.extend(child_episodes)

        # render step after everything is set
        self.render()

        # determine goal for the next step
        # print('new goal ids: {0}'.format(new_goal_ids))
        if len(new_goals) > 1:
            self.cur_subepis.add_new_experience("MANY", self._observe_defaults())
            self.cur_subepis = self.open_subepisodes.pop()
        else:
            self.cur_subepis.add_new_experience(new_goals[0].id, new_goals[0].features)
            self.cur_subepis.cur_goal = new_goals[0]

        # finish regular, non-exiting env step by returning no experience
        return []

# -------------------------------------------------------------------------------------------

    def _episode_exit(self, status, render_status):
        '''
        Handles the end of a (sub-)episode, updates counters and prepares for the next (sub-)episode.
        '''
        self.cur_subepis.status = status
        episode_killed = False

        # restart KeY and consider as 'fail' if episode ended due to KeY problems
        if status == "crash":
            self.connector.restart_key()

        self.finalize_subepisode()
        if status == "fail" or status == 'crash':
            self.po_closable = False # current po cannot be closed anymore.
            
            # end whole root episode on crash or if pre_kill is set to True.
            if status == 'crash' or self.pre_kill:
                while self.open_subepisodes: # status is 'open' for children, 'parent' for parents
                    self.cur_subepis = self.open_subepisodes.pop()
                    self.finalize_subepisode()
                episode_killed = True

        # finish env step, reset the environment and return batched exp, if any.
        self.render(render_status = 'KILLED' if episode_killed else render_status)
        self.reset()
        if len(self.batched_exp) > 0:
            exp_batch, self.batched_exp[:] = self.batched_exp[:], []
            return exp_batch
        return []

    def finalize_subepisode(self):
        '''
        Finalizes a subepisode by filling the corresponding experiences with rewards,
        ending the episode and updating the episode counters.
        '''
        self.rewarder.end_and_reward_subepisode(self.cur_subepis)
        if self.cur_subepis.is_topgoal():
            self.batched_exp.extend(self.cur_subepis.get_experiences())
        self._update_episode_counter()

    def _update_episode_counter(self):
        '''
        Updates the episode counters according to exit status and episode depth.
        '''
        # ignore open subepisodes.
        if self.cur_subepis.status == 'open': return

        self.total_subep_count += 1
        if self.cur_subepis.status == "success": self.successful_subep_count += 1

        # handling for topgoals
        if self.cur_subepis.is_topgoal():
            self.total_topgoal_count += 1
            if self.cur_subepis.status == "success": self.successful_topgoal_count += 1

# -------------------------------------------------------------------------------------------

    def _observe(self):
        """
        Returns a list of feature values from the currently considered PO.
        """
        # get po-side features
        features = self.cur_subepis.cur_goal.features
        # print('features: {0}'.format(features))
        return list(features.values())

    def _observe_defaults(self):
        """
        Returns a list of default feature values.
        """
        default_features = self.extractor.get_feature_defaults()
        return default_features

# -------------------------------------------------------------------------------------------

    def reset(self):
        """
        Resets the environment;
        If there are still open subepisodes: takes the next subepisode.
        Otherwise returns initial observation of ast and features of a random PO.
        """
        # print('reset()::open goals in RL: {0}'.format([se.cur_goal.id for se in self.open_subepisodes]))

        # extra end-of-po work
        if not self.open_subepisodes:

            # the initial reset done by the DQN should only sample a file.
            if self.env_steps > 0:

                self.total_po_count += 1
                
                # all goals went through nicely -> po successful
                if self.po_closable:
                    # assert that KeY doesn't have open goals left either
                    open_goals_in_key = self.connector.get_open_goals()
                    # print('reset()::open goals in KeY: {0}'.format(open_goals_in_key))
                    assert len(open_goals_in_key) == 0, 'KeY still has open goals as opposed to the RL env!'

                    self.successful_po_count += 1
                    self.po_success_history.append(1)

                    if cf.REPRINT_SUCCESSFUL_EPISODES:
                        po_step_count_index = -1 * self.po_steps
                        th_list = list(self.tactic_history)
                        tactic_apps = th_list[po_step_count_index:]
                        self.successful_po_proofs.append((self.cur_po_filepath, tactic_apps))

                # something went wrong -> po failed
                else:
                    self.po_success_history.append(0)

            # sample new file and reset po variables
            self._sample_file()
            self.po_steps = 0
            self.po_closable = True

        # pop new subepisode, regardless of whether new po or not
        self.cur_subepis = self.open_subepisodes.pop()
        return self._observe()

    def _sample_file(self):
        """
        Initializes the environment by loading a random .key file.
        The first open goal is the next subepisode to be visited.
        If multiple goals are loaded, the others are added to the list of
        open subepisodes.
        """

        # get the information for the new po
        datapoints, po_origin_file = self.ob_space.sample()
        self.cur_po_filepath = po_origin_file

        # len(datapoints) is at least 1, otherwise observation_space.sample() would not have returned.
        assert all([self.ob_space.contains(dp) for dp in datapoints]), "observation is not valid"
        goal_nodes = [PONode(str(dp['id']), id=dp['id'], \
            ast=dp['ast'], features=dp['features'], origin=dp['origin']) for dp in datapoints]
        goal_subepisodes = [Subepisode(gn, parent_episode=None) for gn in goal_nodes]
        # print('sample_file()::new goals: {0}'.format([se.cur_goal.id for se in goal_subepisodes]))
        self.open_subepisodes.extend(goal_subepisodes)

# -------------------------------------------------------------------------------------------

    def episode_to_line(self):
        '''
        Returns a compactified information string for the current episode.
        '''
        return "EP.: {epis_steps} steps".format(epis_steps=str(self.po_steps).rjust(5))
        
    def env_to_line(self):  
        '''
        Returns a compactified information string for the environment.
        '''
        return "steps: {n_env} ENV, {n_po} PO  \u2588  \u2713: {suc_po}/{tot_po} PO | {suc_tg}/{tot_tg} TG | {suc_se}/{tot_se} subep"\
            .format(n_env=str(self.env_steps).rjust(6), n_po=str(self.po_steps).rjust(4), tot_po=self.total_po_count,\
                    suc_po=self.successful_po_count, tot_tg=self.total_topgoal_count, suc_tg=self.successful_topgoal_count,\
                    tot_se=self.total_subep_count, suc_se=self.successful_subep_count)

    def render(self, render_status='NON_EXIT'):
        """
        Prints output to trace the learning process.
        """
        open_goals_print = [se.cur_goal.id for se in self.open_subepisodes]
        if len(open_goals_print) > 3: open_goals_print = '[..., ' + str(open_goals_print[-3:])[1:]
        print(self.cur_subepis.to_line(render_status) + '  \u2588  ' + self.env_to_line() + \
            '  \u2588  open: {1} | active: #{0}]'.format(self.cur_subepis.cur_goal.id, str(open_goals_print)[:-1]))