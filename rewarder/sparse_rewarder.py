from rewarder.rewarder import Rewarder

class SparseRewarder(Rewarder):

    def __init__(self, r_ep_end : float, p_ep_end : float, p_step : float):
        '''
        TODO
        '''
        
        self.reward_episode_end = r_ep_end
        self.penalty_episode_end = p_ep_end
        self.step_penalty = p_step

    def end_and_reward_subepisode(self, subepisode):
        '''
        TODO
        '''

        # special treatment for parents
        if len(subepisode.child_episodes) > 0:
            children_steps = sum([cep.steps_taken + cep.children_steps_taken for cep in subepisode.child_episodes])
            subepisode.steps_taken += children_steps
            child_statuses = [ce.status for ce in subepisode.child_episodes]
            subepisode.set_status(self.calculate_success_status(child_statuses))

        if len(subepisode.experiences) < 1: return
        
        rew = self.reward_episode_end if subepisode.status == "success" else self.penalty_episode_end
        leaf_reward = rew - subepisode.steps_taken * self.step_penalty
        subepisode.experiences[-1].reward = leaf_reward
        if len(subepisode.experiences) > 1:
            for exp in subepisode.experiences[:-1]:
                exp.reward = self.step_penalty