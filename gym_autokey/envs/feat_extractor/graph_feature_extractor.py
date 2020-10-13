import anytree
import dgl
import torch
import pyhash

import gym_autokey.envs.feat_extractor.feature_extractor as fe

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

        node_count = 0
        u, v, op_classes, op_name_hashes = [], [], [], []

        node_count, u, v, op_classes, op_name_hashes = self._get_graph_info_recursive(node_count, u, v, op_classes,
                                                                                      op_name_hashes, goal_ast)
        g = dgl.graph((u, v))
        g.ndata["op_classes"] = torch.tensor(op_classes)
        g.ndata["op_names"] = torch.tensor(op_name_hashes)
        return g

    def _get_graph_info_recursive(self, node_count, u, v, op_classes, op_name_hashes, cur_node):
        """
        TODO
        """

        node_count += 1
        op_classes.append(self.hasher(cur_node.op_class))
        op_name_hashes.append(self.hasher(cur_node.op_name))

        for child in cur_node.children:
            u.append(cur_node.id)
            v.append(child.id)
            node_count, u, v, op_classes, op_name_hashes = self._get_graph_info_recursive(node_count, u, v, op_classes,
                                                                                          op_name_hashes, child)
        return node_count, u, v, op_classes, op_name_hashes