from numpy import expm1, float_power

import config as cf
from experience import Experience

class Subepisode():
    '''
    Reinforcement learning as used here assumes a linear control flow during an episode.
    However, the verification of POs can involve splitting into multiple POs,
    which is why each resulting verification sub-process needs to be treated as
    an own episode. This class is able to model such sub-episodes.
    '''
    
    def __init__(self, root_goal, max_steps = cf.ROOT_EPIS_MAX_DEPTH, parent_episode=None):
        '''
        Spawns a subepisode. Subepisodes have the status 'open' upon spawn.
        '''
        self.root_goal = root_goal
        self.cur_goal = root_goal
        self.max_steps = max_steps
        self.parent_episode = parent_episode

        self.steps_taken = 0
        self.children_steps_taken = 0

        self.status = "open"
        self.experiences = [Experience(id=self.cur_goal.id, obs=self.cur_goal.features)]

        self.child_episodes = []
        if self.parent_episode is None:
            self.name = str(self.root_goal.id)
        else:
            self.name = self.parent_episode.name + "-" + str(self.root_goal.id)

    def add_tactic_app_step(self, tactic):
        '''
        Increments the step counter and adds the given tactic to the newest exp.
        '''
        self.steps_taken += 1
        self.experiences[-1].tactic = tactic

    def add_new_experience(self, new_id, new_obs):
        '''
        Assigns new_id and new_obs (if applicable) to this subepisode's current experience.
        '''
        self.experiences[-1].new_id = new_id
        self.experiences[-1].new_obs = new_obs
        self.experiences.append(Experience(id=new_id, obs=new_obs))

    def set_status(self, status):
        '''
        Sets the current status.
        '''
        self.status = status

    def max_steps_reached(self):
        '''
        Returns True if no more steps can be taken, False otherwise.
        '''
        return self.steps_taken >= self.max_steps

    def steps_remaining(self):
        '''
        Returns the number of steps that can still be taken.
        '''
        return self.max_steps - self.steps_taken

    def is_topgoal(self):
        '''
        Returns True if the subepisode is the start of a topgoal (no parent),
        False otherwise.
        '''
        return self.parent_episode is None

    def become_parent(self, child_episodes):
        '''
        Sets the status of this subepisode to parent and hands over given child episodes.
        '''
        self.child_episodes = child_episodes
        self.set_statue("parent")

    def to_line(self, render_status):
        '''
        Returns a compactified information string for this subepisode.
        '''
        
        if render_status == 'EXIT_PRE_EXECUTION': tactic_str = '------' 
        elif render_status == 'KILLED': tactic_str = 'KILLED'
        else: tactic_str = tactic_str = self.experiences[-1].tactic + ' on #' + str(self.cur_goal.id)
        return "[{steps_taken}/{max_steps}, {status}] {tactic}"\
            .format(steps_taken=str(self.steps_taken).rjust(len(str(cf.ROOT_EPIS_MAX_DEPTH))),\
                    max_steps=str(self.max_steps).rjust(len(str(cf.ROOT_EPIS_MAX_DEPTH))),\
                    status=self.status[0:4], tactic=str(tactic_str).rjust(22))

    def to_tree(self, pre=''):
        '''
        Returns a string contaning a tree representation of this subepisode.
        '''
        cur_subep_str =  "{pre} [#{goal_id}] {status} | {st} steps"\
        .format(pre=pre, goal_id=self.cur_goal.id, status=self.status[0], st=self.steps_taken)
        children_eps = ["{0}".format(child.to_tree('  ' + pre)) for child in self.child_episodes]
        for c in children_eps:
            cur_subep_str += c
        return cur_subep_str
    
    def get_experiences(self):
        experiences = self.experiences
        for child in self.child_episodes:
            experiences.extend(child.get_experiences())
        return experiences