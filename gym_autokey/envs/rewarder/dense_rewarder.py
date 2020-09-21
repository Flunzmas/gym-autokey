from gym_autokey.envs.rewarder.rewarder import Rewarder


class DenseRewarder(Rewarder):
    """
    The dense rewarder tries to provide rewards more frequently than just on
    proof completion or time-out.
    """

    def __init__(self, r_ep_end: float, p_ep_end: float, p_step: float):
        self.reward_episode_end = r_ep_end
        self.penalty_episode_end = p_ep_end
        self.step_penalty = p_step

    def end_and_reward_subepisode(self, subepisode):
        """
        TODO implement
        """
        raise NotImplementedError