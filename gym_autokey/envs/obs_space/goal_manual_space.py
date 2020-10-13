import gym_autokey.envs.obs_space.goal_space as gs
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.feat_extractor.manual_feat_extractor as mfe


class GoalManualSpace(gs.GoalSpace):
    """This subclass of GoalSpace deals with a feature dictionary containing hand-picked features."""

    def __init__(self, connector : kc.KeYConnector, extractor : mfe.ManualFeatExtractor):
        super(GoalManualSpace, self).__init__(connector, extractor)

    def contains(self, x):
        """returns True if given goal features are smaller than 1 and larger than -1."""
        if not isinstance(x, dict):
            return False
        feat_values = list(x.values())
        if any([f > 1 for f in feat_values]) or any([f < -1 for f in feat_values]):
            return False
        return True


    def render(self, obs : dict):
        """Prints the features dict to the console."""
        print("features:")
        for key in obs.keys():
            print("\t{0}: {1}".format(key, obs[key]))