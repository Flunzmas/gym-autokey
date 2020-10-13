import gym_autokey.envs.feature_extractor.hand_picked_feature_extractor as hfe
import gym_autokey.envs.feature_extractor.graph_feature_extractor as gfe
import gym_autokey.envs.config as cf

def create_feature_extractor():
    """
    TODO
    """

    if cf.FEATURE_MODE == "manual":
        return hfe.HandPickedFeatureExtractor()
    elif cf.FEATURE_MODE == "graph":
        return gfe.GraphFeatureExtractor()
    else:
        return hfe.HandPickedFeatureExtractor()