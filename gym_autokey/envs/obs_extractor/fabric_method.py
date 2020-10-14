import gym_autokey.envs.obs_extractor.manual_feat_obs_extractor as hfe
import gym_autokey.envs.obs_extractor.graph_obs_extractor as gfe
import gym_autokey.envs.obs_extractor.full_text_obs_extractor as ftfe
import gym_autokey.envs.obs_extractor.name_only_text_obs_extractor as ntfe
import gym_autokey.envs.config as cf


def create_feature_extractor():
    """
    Returns a feature extractor depending on the feature mode set in the config file.
    """
    return feature_extractors.get(cf.FEATURE_MODE, hfe.ManualFeatObsExtractor())


feature_extractors = {
    "manual": hfe.ManualFeatObsExtractor(),
    "graph": gfe.GraphObsExtractor(),
    "text_full": ftfe.FullTextObsExtractor(),
    "text_names": ntfe.NameOnlyTextObsExtractor(),
}
