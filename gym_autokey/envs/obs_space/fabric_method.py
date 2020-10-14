import gym_autokey.envs.obs_space.goal_graph_space as ggs
import gym_autokey.envs.obs_space.goal_manual_space as gms
import gym_autokey.envs.obs_space.goal_text_space as gts
import gym_autokey.envs.config as cf
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.obs_extractor.obs_extractor as fe


def create_goal_space(connector: kc.KeYConnector, extractor: fe.ObsExtractor):
    """
    Returns a goal space depending on the feature mode set in the config file.
    """
    space = obs_spaces.get(cf.FEATURE_MODE, gms.GoalManualSpace)
    return space(connector, extractor)


obs_spaces = {
    "manual": gms.GoalManualSpace,
    "graph": ggs.GoalGraphSpace,
    "text_full": gts.GoalTextSpace,
    "text_names": gts.GoalTextSpace
}
