import anytree
import dgl
import torch

import numpy as np
import gym_autokey.envs.obs_extractor.obs_extractor as fe


class GraphObsExtractor(fe.ObsExtractor):

    def obs_from_anytree(self, goal_ast: anytree.Node):
        """
        Creates a dgl graph as the features.
        """

        node_count = 0
        u, v = [], []
        op_classes = []

        node_count, u, v, op_classes = self._get_graph_info_recursive(node_count, u, v, op_classes, goal_ast)
        g = dgl.graph((u, v))
        g.ndata["op_classes"] = torch.cat(op_classes, dim=0)
        return g

    def _get_graph_info_recursive(self, node_count, u, v, op_classes, cur_node):
        """
        Recursive method used for the DGLGraph generation to traverse the anytree.
        """

        node_count += 1
        # one-hot encoded vector containing the op_class of the node
        op_class = torch.from_numpy(np.eye(len(op_class_to_idx.keys()))[op_class_to_idx[cur_node.op_class]])
        op_classes.append(op_class.unsqueeze(0))

        for child in cur_node.children:
            u.append(cur_node.id)
            v.append(child.id)
            node_count, u, v, op_classes = self._get_graph_info_recursive(node_count, u, v, op_classes, child)
        return node_count, u, v, op_classes


op_class_to_idx = {
    "UpdateApplication": 0,
    "UpdateJunctor": 1,
    "ElementaryUpdate": 2,
    "Function": 3,
    "LocationVariable": 4,
    "Equality": 5,
    "Junctor": 6,
    "SortDependingFunction": 7,
    "Modality": 8,
    "Quantifier": 9,
    "LogicVariable": 10,
    "ObserverFunction": 11,
    "ProgramMethod": 12,
    "IfThenElse": 13,
    "ProgramConstant": 14,
    "RootImp": 15
}
