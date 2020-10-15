import time
from collections import deque

import gym
from gym import spaces

import gym_autokey.envs.config as cf
from gym_autokey.envs.subepisode import Subepisode
from gym_autokey.envs.key_connector import KeYConnector
from gym_autokey.envs.obs_extractor.fabric_method import create_feature_extractor
from gym_autokey.envs.obs_space.fabric_method import create_goal_space
from gym_autokey.envs.datastructures.po_node import PONode
from gym_autokey.envs.datastructures.fixed_length_deque import FixedLengthDeque


class AutokeyEnv(gym.Env):
    """
    This environment executes deductive proofs in KeY. Each of its actions is a
    bundle of deduction rules (called 'tactic') applied to the currently active
    goal. In order to successfully conduct the proof for a given proof
    obligation, all of its underlying goals have to be proven using such tactic
    applications. Rewards correspond to the resulting proving performance and
    the amount of closed goals and POs.
    """

    metadata = {'render.modes': ['human']}

    def __init__(self, self_render: bool = True):
        """
        Initializes all env variables necessary for running the environment.
        Properly initializes the KeY Connector and sets up counters etc.
        """
        print("NO_SMT: {}".format(str(cf.NO_SMT)))
        print("FEATURE_MODE: {}".format(cf.FEATURE_MODE))

        self.connector = KeYConnector()
        self.extractor = create_feature_extractor()
        self.obs_space = create_goal_space(self.connector, self.extractor)
        self.ac_space = spaces.Discrete(len(self.connector.available_tactics))

        self.max_steps_per_po = cf.MAX_STEPS_PER_PO
        self.pre_kill = cf.PRE_KILL_FAILED_EPISODES
        self.self_render = self_render
        if self_render:
            print("AutokeyEnv: self_render is set to True!")
        self.initial_reset = False

        self.env_steps = 0
        self.po_steps = 0  # number of steps for the current po (the currently loaded file)
        self.po_closable = True  # while proving a PO, set this to false as soon as anything fails. Reset for new PO.
        self.topgoal_done = False  # returned on episode_exit and set to False if PO is still running
        self.topgoal_rew = 0  # returned on episode_exit and set to 0 if PO is still running
        self.last_action = "---"
        self.po_percent_logfile = (
                cf.LOG_PATH / ("po_percentage_" + time.strftime("%d%m%Y-%H%M%S") + ".txt")).as_posix()

        self.po_success_history = FixedLengthDeque(cf.POWISE_BUFFER_SIZE)
        self.reward_history = FixedLengthDeque(cf.STEPWISE_BUFFER_SIZE)
        self.tactic_history = FixedLengthDeque(cf.STEPWISE_BUFFER_SIZE)

        # if cf.REPRINT_SUCCESSFUL_EPISODES is set to True,
        # this list contains the proving history of all successfully closed POs.
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

        self.cur_subepis = None
        # self.reset() # if needed

    def _del(self):
        """
        To be run on deletion/program end: closes KeY.
        """
        if self.connector:
            self.connector.quit_key()

# -------------------------------------------------------------------------------------------

    def step(self, action):
        """
        Executes a learning step: maps given action to a tactic, executes it
        and observes what is happening. Deals with all the eventualities of the
        proving process.
        """

        # time.sleep(0.3)
        if self.env_steps == 3:
            print(self._observe())

        # render the past step at the beginning if self_render
        if self.self_render:
            self.render() 
        if self.env_steps % 1000 == 0 and len(self.po_success_history) > 0:
            with open(self.po_percent_logfile, 'a') as po_p_file:
                po_p_file.write(str(round(100 * sum(self.po_success_history) / len(self.po_success_history), 1)) + "\n")

        # self.print_open_goals("step")

        # initializing bookkeeping
        cur_tactic_app_str = "(#{id})".format(id=self.cur_subepis.cur_goal.id).rjust(13)
        self.topgoal_done = False
        self.topgoal_rew = 0
        self.env_steps += 1

        # pre-execution episode checks
        if self.cur_subepis.child_episodes:
            self.last_action = "splitting:" + cur_tactic_app_str
            return self._episode_exit("parent")
        if self.po_steps > self.max_steps_per_po:
            self.last_action = "out of PO steps:" + cur_tactic_app_str
            return self._episode_exit("crash")
        if self.cur_subepis.max_steps_reached():
            self.last_action = "tree depth reached:" + cur_tactic_app_str
            return self._episode_exit("fail")

        self.po_steps += 1

        # tactic preparation
        cur_tactic = self.connector.available_tactics[action]
        cur_tactic_app_str = ' {0}'.format(cf.TACTIC_ABBR[cur_tactic])\
                             + ' (#{0})'.format(self.cur_subepis.cur_goal.id).rjust(9)
        self.tactic_history.append(cur_tactic_app_str)

        # tactic execution
        cur_goal_id = self.cur_subepis.cur_goal.id
        new_goal_ids = self.connector.execute_tactic(cur_goal_id, cur_tactic)
        self.cur_subepis.add_tactic_app_step(cur_tactic)

        # post-execution episode checks
        if not new_goal_ids:
            self.last_action = "closed:" + cur_tactic_app_str
            return self._episode_exit("success")
        if new_goal_ids == [-1]:
            self.last_action = "crash (ids == [-1]):" + cur_tactic_app_str
            return self._episode_exit("crash")

        # Create nodes from the resulting PO IDs
        new_goals = []
        for goal_id in new_goal_ids:
            new_goal_node = PONode.from_id(goal_id, self.connector, self.extractor, parent=self.cur_subepis.cur_goal)
            # to catch those rare cases of failed node creations
            if new_goal_node.id == -1:
                self.last_action = "crash (an id is -1):" + cur_tactic_app_str
                return self._episode_exit("crash")
            new_goals.append(new_goal_node)
                 
        # Multiple new goals -> subepisode becomes parent and env continues with a subepisode
        if len(new_goals) > 1:
            child_episodes = [
                Subepisode(root_goal=new_goal, max_steps=self.cur_subepis.steps_remaining(),
                           parent_episode=self.cur_subepis) for new_goal in new_goals]
            self.cur_subepis.become_parent(child_episodes)
            # append newly made parent to open subepisode stack, followed by its children
            self.open_subepisodes.append(self.cur_subepis)
            self.open_subepisodes.extend(child_episodes)

        # determine goal for the next step
        # print('new goal ids: {0}'.format(new_goal_ids))
        if len(new_goals) > 1:
            # if self.cur_subepis is not None:
            #     print("multiple new goals_prePop: {}".format(self.cur_subepis.cur_goal.id))
            self.cur_subepis = self.open_subepisodes.pop()
        else:
            self.cur_subepis.cur_goal = new_goals[0]

        self.last_action = "open:" + cur_tactic_app_str
        return self._observe(), 0, False, {}  # return obs, rew, done, infos

# -------------------------------------------------------------------------------------------

    def _episode_exit(self, status):
        """
        Handles the end of a (sub-)episode, updates counters and prepares for the next (sub-)episode.
        """

        self.cur_subepis.status = status

        # restart KeY and consider as 'fail' if episode ended due to KeY problems
        if status == "crash":
            self.connector.restart_key()

        self.finalize_subepisode()
        if status == "fail" or status == 'crash':
            self.po_closable = False  # current po cannot be closed anymore.
            
            # end whole root episode on crash or if pre_kill is set to True.
            if status == 'crash' or self.pre_kill:
                while self.open_subepisodes:  # status is 'open' for children, 'parent' for parents
                    # if self.cur_subepis is not None:
                    #     print("killing_subep_prePop: {}".format(self.cur_subepis.cur_goal.id))
                    self.cur_subepis = self.open_subepisodes.pop()
                    self.finalize_subepisode()

        # finish env step, reset the environment.
        obs = self.reset(exit_status=status)
        rew = self.topgoal_rew
        done = self.topgoal_done
        return obs, rew, done, {}  # obs, rew, done, infos

    def finalize_subepisode(self):
        """
        Finalizes a subepisode by ending the episode and updating the episode counters.
        Determine the due reward by checking the termination status of all concerned subepisodes of given topgoal.
        """

        # status treatment for parents
        if len(self.cur_subepis.child_episodes) > 0:
            children_steps = sum(
                [cep.steps_taken + cep.children_steps_taken
                 for cep in self.cur_subepis.child_episodes])
            self.cur_subepis.steps_taken += children_steps
            ch_statues = [ce.status for ce in self.cur_subepis.child_episodes]

            if ch_statues == {'success'}:
                self.cur_subepis.status = "success"
            elif "crash" in ch_statues:
                self.cur_subepis.status = "crash"
            else:
                self.cur_subepis.status = "fail"

        self._update_episode_counter()

    def _update_episode_counter(self):
        """
        Updates the episode counters according to exit status and episode depth.
        """
        # ignore open subepisodes.
        if self.cur_subepis.status == 'open':
            return

        self.total_subep_count += 1
        if self.cur_subepis.status == "success":
            self.successful_subep_count += 1

        # handling for topgoals
        if self.cur_subepis.is_topgoal():
            self.total_topgoal_count += 1
            if self.cur_subepis.status == "success":
                self.successful_topgoal_count += 1

# -------------------------------------------------------------------------------------------

    def _observe(self):
        """
        Returns the current goal's features.
        """
        obs = self.cur_subepis.cur_goal.features
        # self.obs_space.render(obs)
        return obs

# -------------------------------------------------------------------------------------------

    def reset(self, exit_status="none"):
        """
        Resets the environment;
        If there are still open subepisodes: takes the next subepisode.
        Otherwise returns initial observation of ast and features of a random PO.
        """
        
        # print("reset():: {}".format(exit_status))
        # self.print_open_goals("reset")

        # reset() calls with default status 'none' should not affect the env if it was initially reset.
        if exit_status == "none":
            if self.initial_reset:
                # print("reset():: non-initial external reset, ignored.")
                return self._observe()
            else:
                # print("reset():: initial external reset!")
                self.initial_reset = True

        # extra end-of-po work
        if not self.open_subepisodes:

            # the initial reset done by the DQN should only sample a file.
            if self.env_steps > 0:

                self.total_po_count += 1
                
                # all goals went through nicely -> po successful
                if self.po_closable:
                    # assert that KeY doesn't have open goals left either
                    assert len(self.connector.get_open_goals()) == 0, \
                        'KeY still has open goals as opposed to the RL env!'

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

        # extra end-of-topgoal work
        if self.cur_subepis is not None and self.cur_subepis.is_topgoal():
            if exit_status == "success":
                self.topgoal_done = True
                self.topgoal_rew = cf.REWARD_EPISODE_END
            elif exit_status == "fail" or exit_status == "crash":
                self.topgoal_done = True
                self.topgoal_rew = cf.PENALTY_EPISODE_END

        # pop new subepisode, regardless of whether new po or not
        # if self.cur_subepis is not None: print("killing_subep_prePop: {}".format(self.cur_subepis.cur_goal.id))
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
        datapoints, po_origin_file = self.obs_space.sample()
        self.cur_po_filepath = po_origin_file

        # len(datapoints) is at least 1, otherwise observation_space.sample() would not have returned.
        assert all([dp["id"] >= 0 for dp in datapoints]), "non-valid IDs in resulting dps"
        assert all([self.obs_space.contains(dp["features"]) for dp in datapoints]), "non-valid features"
        goal_nodes = [
            PONode(str(dp['id']), id=dp['id'], ast=dp['ast'], features=dp['features'], origin=dp['origin'])
            for dp in datapoints]
        goal_subepisodes = [Subepisode(gn, parent_episode=None) for gn in goal_nodes]
        # print('sample_file()::new goals: {0}'.format(sorted([se.cur_goal.id for se in goal_subepisodes])))
        self.open_subepisodes.extend(goal_subepisodes)

# -------------------------------------------------------------------------------------------

    def episode_to_line(self):
        """
        Returns a compactified information string for the current episode.
        """
        return "EP.: {epis_steps} steps".format(epis_steps=str(self.po_steps).rjust(5))
        
    def env_to_line(self):  
        """
        Returns a compactified information string for the environment.
        """
        po_done_str = "F, 0"
        if self.topgoal_done:
            if self.topgoal_rew < 0:
                po_done_str = "T, -"
            elif self.topgoal_rew > 0:
                po_done_str = "T, +"
        po_percent = str(round(100 * sum(self.po_success_history) / len(self.po_success_history), 1)).rjust(5) \
            if len(self.po_success_history) > 0 else "-----"

        return "{ac} \u2588 step: {n_env} ENV, {n_po} PO \u2588"\
            " \u2713: {suc_po}/{tot_po} PO | {suc_tg}/{tot_tg} TG | {suc_se}/{tot_se} subep"\
            " \u2588 PO%: {po_p} \u2588 TG_done: {po_dn}"\
            .format(
                ac=self.last_action.rjust(34), n_env=str(self.env_steps).rjust(6),
                n_po=str(self.po_steps).rjust(4), tot_po=self.total_po_count, suc_po=self.successful_po_count,
                tot_tg=self.total_topgoal_count, suc_tg=self.successful_topgoal_count, tot_se=self.total_subep_count,
                suc_se=self.successful_subep_count, po_p=po_percent, po_dn=po_done_str.rjust(4))

    def render(self, mode='human'):
        """
        Prints output to trace the learning process.
        """
        open_goals_print = [se.cur_goal.id for se in self.open_subepisodes]
        if len(open_goals_print) > 3:
            open_goals_print = '[..., ' + str(open_goals_print[-3:])[1:]
        print(
            self.env_to_line() + '  \u2588  now open: {1} | active: #{0}]'
            .format(self.cur_subepis.cur_goal.id, str(open_goals_print)[:-1]))

    def print_open_goals(self, origin_func: str):
        """
        Prints two lines containing the currently open goal numbers, both in KeY and the env.
        """
        og_rl = [se.cur_goal.id for se in self.open_subepisodes]
        if self.cur_subepis is not None:
            og_rl.append(self.cur_subepis.cur_goal.id)
        og_key = self.connector.get_open_goals()
        print('{0}()::open goals in RL : {1}'.format(origin_func, sorted(og_rl)))
        print('{0}()::open goals in KeY: {1}'.format(origin_func, sorted(og_key)))
