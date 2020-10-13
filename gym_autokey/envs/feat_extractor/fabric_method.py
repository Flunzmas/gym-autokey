import gym_autokey.envs.feat_extractor.manual_feature_extractor as hfe
import gym_autokey.envs.feat_extractor.graph_feature_extractor as gfe
import gym_autokey.envs.config as cf

def create_feature_extractor():
    """
    TODO
    """

    if cf.FEATURE_MODE == "manual":
        return hfe.ManualFeatureExtractor()
    elif cf.FEATURE_MODE == "graph":
        return gfe.GraphFeatureExtractor()
    else:
        return hfe.ManualFeatureExtractor()