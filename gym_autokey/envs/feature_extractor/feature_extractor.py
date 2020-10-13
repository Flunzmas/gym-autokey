import anytree

import gym_autokey.envs.config as cf
import gym_autokey.envs.feature_extractor.hand_picked_feature_extractor as hfe
import gym_autokey.envs.feature_extractor.graph_feature_extractor as gfe

class FeatureExtractor(object):
    """
    TODO
    """

    fe = None

    def __init__(self):
        if cf.FEATURE_MODE == "manual":
            self.fe = hfe.HandPickedFeatureExtractor()
        elif cf.FEATURE_MODE == "graph":
            self.fe = gfe.GraphFeatureExtractor()
        else:
            self.fe = hfe.HandPickedFeatureExtractor()


    def extract_features(self, goal_ast : anytree.Node):
        """
        TODO
        """
        return self.fe.extract_features(goal_ast)