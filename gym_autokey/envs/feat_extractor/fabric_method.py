import gym_autokey.envs.feat_extractor.manual_feat_extractor as hfe
import gym_autokey.envs.feat_extractor.graph_feat_extractor as gfe
import gym_autokey.envs.config as cf

def create_feature_extractor():
    """
    Returns a feature extractor depending on the feature mode set in the config file.
    """

    if cf.FEATURE_MODE == "manual":
        return hfe.ManualFeatExtractor()
    elif cf.FEATURE_MODE == "graph":
        return gfe.GraphFeatExtractor()
    else:
        return hfe.ManualFeatExtractor()