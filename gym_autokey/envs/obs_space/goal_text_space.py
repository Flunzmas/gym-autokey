import gym_autokey.envs.obs_space.goal_space as gs
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.obs_extractor.text_obs_extractor as toe


class GoalTextSpace(gs.GoalSpace):
    """This subclass of GoalSpace deals with the AST represented as a string."""

    def __init__(self, connector: kc.KeYConnector, extractor: toe.TextObsExtractor):
        super(GoalTextSpace, self).__init__(connector, extractor)

    def contains(self, x):
        """
        Check whether x is a string.
        """
        return isinstance(x, str)

    def render(self, obs : str):
        """Prints the obs text to the console."""

        print(obs)