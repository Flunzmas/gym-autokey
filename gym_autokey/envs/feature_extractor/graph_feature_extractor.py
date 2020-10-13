import anytree
import dgl
import torch
import pyhash
import time

import gym_autokey.envs.feature_extractor.feature_extractor as fe
import gym_autokey.envs.datastructures.dgl_goal_graph as dgg

class GraphFeatureExtractor(fe.FeatureExtractor):

    hasher = None

    def __init__(self):
        """
        TODO
        """

        # hashing to more than 32 bits results in an overflow error with torch.tensor
        self.hasher = pyhash.fnv1_32(seed=42)

    def extract_features(self, goal_ast : anytree.Node):
        """
        TODO
        """

        graph = dgg.dgl_graph_from_anytree(self.hasher, goal_ast)

        features = None
        raise NotImplementedError() # TODO
        return features