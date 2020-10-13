import numpy as np
import gym
import torch
import pyhash

import anytree
import dgl
import networkx as nx
import matplotlib.pyplot as plt

import gym_autokey.envs.config as cf
import gym_autokey.envs.po_loader as pl
import gym_autokey.envs.obs_space.goal_space as gs
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.feat_extractor.graph_feature_extractor as gfe


class GoalGraphSpace(gs.GoalSpace):
    """
    TODO
    """

    hasher = None

    def __init__(self, connector: kc.KeYConnector, extractor: gfe.GraphFeatureExtractor):
        super(GoalGraphSpace, self).__init__(connector, extractor)

        self.hasher = pyhash.fnv1_32(seed=42)  # >32 bits -> overflow error with torch.tensor

    def contains(self, x):
        """
        TODO
        """
        return isinstance(x, dgl.DGLGraph)

    def render(self, obs):
        """
        TODO
        """

        # it's either dict or DGLGraph.
        if isinstance(obs, dict):
            obs = obs["features"]

        obs_nx = obs.to_networkx()
        pos = nx.kamada_kawai_layout(obs_nx)

        fig, ax = plt.subplots()
        ax.set_title("Graph")
        nx.draw_networkx(obs_nx, pos, ax=ax)
        nx.draw_networkx_labels(obs_nx, pos, ax=ax)
        plt.show()