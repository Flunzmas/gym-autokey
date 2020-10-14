import anytree

class ObsExtractor(object):
    """
    Base class for all feature extractors.
    """

    def __init__(self):
        pass

    def obs_from_anytree(self, goal_ast : anytree.Node):
        raise NotImplementedError
