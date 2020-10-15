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
        u, v, op_class_ids, op_class_one_hots = [], [], [], []

        node_count, u, v, op_class_ids, op_class_one_hots = self._get_graph_info_recursive(goal_ast, node_count, u, v,
                                                                                           op_class_ids,
                                                                                           op_class_one_hots)
        g = dgl.graph((u, v))
        g.ndata["id"] = torch.arange(node_count)
        g.ndata["op_class_id"] = torch.cat(op_class_ids, dim=0)
        g.ndata["op_class_one_hot"] = torch.cat(op_class_one_hots, dim=0)
        return g

    def _get_graph_info_recursive(self, cur_node, node_count, u, v, op_class_ids, op_class_one_hots):
        """
        Recursive method used for the DGLGraph generation to traverse the anytree.
        """

        node_count += 1
        # one-hot encoded vector containing the op_class of the node
        op_class_id = op_class_to_idx[cur_node.op_class]
        op_class_ids.append(torch.unsqueeze(torch.tensor(op_class_id), 0))
        op_class_one_hot = torch.from_numpy(np.eye(len(op_class_to_idx.keys()))[op_class_id])
        op_class_one_hots.append(torch.unsqueeze(op_class_one_hot, 0))

        for child in cur_node.children:
            u.append(cur_node.id)
            v.append(child.id)

            node_count, u, v, op_class_ids, op_class_one_hots = self._get_graph_info_recursive(child, node_count, u, v,
                                                                                               op_class_ids,
                                                                                               op_class_one_hots)
        return node_count, u, v, op_class_ids, op_class_one_hots


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
