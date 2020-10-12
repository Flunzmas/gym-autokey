import dgl
import torch
import pyhash
import time
import networkx as nx
import matplotlib.pyplot as plt

def dgl_graph_from_anytree(root_node):
    """
    TODO
    """

    node_count = 0
    u, v, op_classes, op_name_hashes = [], [], [], []
    hasher = pyhash.fnv1_32(seed=42)  # hashing to more than 32 bits results in an overflow error with torch.tensor

    node_count, u, v, op_classes, op_name_hashes = _get_graph_info_recursive(node_count, u, v, op_classes,
                                                                             op_name_hashes, root_node, hasher)
    g = dgl.graph((u, v))
    g.ndata["op_classes"] = torch.tensor(op_classes)
    g.ndata["op_names"] = torch.tensor(op_name_hashes)
    return g

def _get_graph_info_recursive(node_count, u, v, op_classes, op_name_hashes, cur_node, hasher):
    """
    TODO
    """

    node_count += 1
    op_classes.append(hasher(cur_node.op_class))
    op_name_hashes.append(hasher(cur_node.op_name))

    for child in cur_node.children:
        u.append(cur_node.id)
        v.append(child.id)
        node_count, u, v, op_classes, op_name_hashes = _get_graph_info_recursive(node_count, u, v, op_classes,
                                                                                 op_name_hashes, child, hasher)
    return node_count, u, v, op_classes, op_name_hashes

def draw_graph(graph: dgl.DGLGraph):
    """
    TODO
    """

    graph_nx = graph.to_networkx()
    pos = nx.kamada_kawai_layout(graph_nx)

    fig, ax = plt.subplots()
    ax.set_title("Graph")
    nx.draw_networkx(graph_nx, pos, ax=ax)
    nx.draw_networkx_labels(graph_nx, pos, ax=ax)
    plt.show()