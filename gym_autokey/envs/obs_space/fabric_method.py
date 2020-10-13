import gym_autokey.envs.obs_space.goal_graph_space as ggs
import gym_autokey.envs.obs_space.goal_manual_space as gms
import gym_autokey.envs.config as cf
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.feat_extractor.feature_extractor as fe

def create_goal_space(connector : kc.KeYConnector, extractor : fe.FeatureExtractor):
    """
    TODO
    """

    if cf.FEATURE_MODE == "manual":
        return gms.GoalManualSpace(connector, extractor)
    elif cf.FEATURE_MODE == "graph":
        return ggs.GoalGraphSpace(connector, extractor)
    else:
        return gms.GoalManualSpace(connector, extractor)