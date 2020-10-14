import gym
import anytree

import gym_autokey.envs.config as cf
import gym_autokey.envs.po_loader as pl
import gym_autokey.envs.key_connector as kc
import gym_autokey.envs.obs_extractor.obs_extractor as fe


class GoalSpace(gym.Space):
    """
    The observation space is the space of all valid goal sequents.
    The subclasses each define how the sequents themselves are represented.
    However, all subclasses share the logic to sample as this requires contacting KeY.
    Technically, sampling is done by loading a random file from the (big) list of loadable keyroot_id_listiles.
    """

    def __init__(self, connector: kc.KeYConnector, extractor: fe.ObsExtractor):
        self.connector = connector
        self.extractor = extractor

    def sample(self):
        """
        Loads a random PO data point from the data point collection and retrieves its data.
        The features themselves are returned encapsulated in a dict that also includes the goal id and other info.
        """
        failed_attempts = 0
        while True:

            # restart KeY if loading seems to be broken
            if failed_attempts >= cf.MAX_FAILED_LOADING_ATTEMPTS:
                print("{0} failed loading attempts, restarting key...".format(failed_attempts))
                self.connector.restart_key()
                failed_attempts = 0

            selected_goal_ids, po_origin_file = pl.load_data_point(self.connector)
            if len(selected_goal_ids) < 1:
                print('load_data_point returned no goal ids!')
                failed_attempts += 1
                continue

            dps = []
            for goal_id in selected_goal_ids:
                dp = dict()
                dp['id'] = goal_id
                dp['origin'] = po_origin_file
                dp['ast'] = self.connector.get_goal_ast(dp['id'])
                dp['features'] = self.extractor.obs_from_anytree(dp['ast'])
                dps.append(dp)

            # return dps if everything went well
            return dps, po_origin_file

    def render(self, obs):
        """
        Visualizes the given features (implemented by the subclasses).
        """
        raise NotImplementedError
