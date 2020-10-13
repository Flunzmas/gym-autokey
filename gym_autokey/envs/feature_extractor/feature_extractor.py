import anytree

class FeatureExtractor(object):
    """
    TODO
    """

    def __init__(self):
        pass

    def get_feature_count(self):
        raise NotImplementedError()

    def extract_features(self, goal_ast : anytree.Node):
        raise NotImplementedError()
