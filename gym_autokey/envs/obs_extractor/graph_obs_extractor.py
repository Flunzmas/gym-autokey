import anytree
import dgl
import torch

import numpy as np
import gym_autokey.envs.obs_extractor.obs_extractor as fe

# one extra category for unknown classes
op_class_to_idx = {
    "Unknown": 0,
    "RootImp": 1,
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
    "UpdateApplication": 15,
    "UpdateJunctor": 16
}

op_class_count = len(op_class_to_idx)


class GraphObsExtractor(fe.ObsExtractor):

    def obs_from_anytree(self, goal_ast: anytree.Node):
        """
        Creates a dgl graph as the features.
        """

        node_count = 0
        u, v, op_class_ids, op_class_one_hots, norms = [], [], [], [], []

        node_count, u, v, op_class_ids, op_class_one_hots, norms \
            = self._get_graph_info_recursive(goal_ast, node_count, u, v, op_class_ids, op_class_one_hots, norms)

        g = dgl.graph((u, v))
        g.ndata["id"] = torch.arange(node_count)
        g.ndata["op_class_id"] = torch.cat(op_class_ids, dim=0)
        g.ndata["op_class_one_hot"] = torch.cat(op_class_one_hots, dim=0)
        g.ndata["norm"] = torch.cat(norms, dim=0)
        return g

    def _get_graph_info_recursive(self, cur_node, node_count, u, v, op_class_ids, op_class_one_hots, norms):
        """
        Recursive method used for the DGLGraph generation to traverse the anytree.
        """

        node_count += 1

        # one-hot encoded vector containing the op_class of the node
        op_class_id = op_class_to_idx.get(cur_node.op_class, op_class_to_idx["Unknown"])
        op_class_ids.append(torch.unsqueeze(torch.tensor(op_class_id), 0))
        op_class_one_hot = torch.from_numpy(np.eye(len(op_class_to_idx.keys()), dtype=np.float32)[op_class_id])
        op_class_one_hots.append(torch.unsqueeze(op_class_one_hot, 0))

        # also counting the current node itself and creating an edge to itself
        child_count = 1
        u.append(cur_node.id)
        v.append(cur_node.id)

        for child in cur_node.children:
            child_count += 1
            u.append(cur_node.id)
            v.append(child.id)

            node_count, u, v, op_class_ids, op_class_one_hots, norms \
                = self._get_graph_info_recursive(child, node_count, u, v, op_class_ids, op_class_one_hots, norms)

        # normalization constant per node depends on number of child nodes
        norms.append(torch.unsqueeze(torch.tensor(1 / child_count, dtype=torch.float32), 0))
        return node_count, u, v, op_class_ids, op_class_one_hots, norms
