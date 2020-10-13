import anytree

class FeatExtractor(object):
    """
    Base class for all feature extractors.
    """

    def __init__(self):
        pass

    def extract_features(self, goal_ast : anytree.Node):
        raise NotImplementedError
