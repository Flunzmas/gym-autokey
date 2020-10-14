import pyhash
import dgl
import networkx as nx
import matplotlib.pyplot as plt

import gym_autokey.envs.obs_space.goal_space as gs
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.obs_extractor.graph_obs_extractor as gfe


class GoalGraphSpace(gs.GoalSpace):
    """This subclass of GoalSpace deals with a feature DGLGraph."""

    def __init__(self, connector: kc.KeYConnector, extractor: gfe.GraphObsExtractor):
        super(GoalGraphSpace, self).__init__(connector, extractor)

    def contains(self, x):
        """
        Check whether x is a valid goal DGLGraph.
        """
        return isinstance(x, dgl.DGLGraph)

    def render(self, obs):
        """
        Show a plot visualizing given DGLGraph.
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