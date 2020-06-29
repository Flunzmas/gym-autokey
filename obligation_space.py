import os
import random

import numpy as np
import tensorflow as tf
from gym.spaces.box import Box

import config as cf
from datastructures.po_anytree import validate_anytree
import po_loader as pl


class ObligationSpace(Box):
    """The observation space is the space of all valid proof obligation formulas.
    Technically, sampling is done by loading a random
    file from the (big) list of loadable keyroot_id_listiles. 
    """
    def __init__(self, connector, extractor):
        self.connector = connector
        self.extractor = extractor
        bounds_low = [-1.0 for i in range (self.extractor.feature_count)]
        bounds_high = [1.0 for i in range (self.extractor.feature_count)]
        super(ObligationSpace, self).__init__(low=np.array(bounds_low), high=np.array(bounds_high), dtype=np.float64)

    def sample(self):
        """Loads a random PO data point from the data
        point collection and retrieves its data."""
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
                try: # ...to retrieve AST and features
                    dp['ast'] = self.connector.get_obligation_ast(dp['id'])
                    dp['features'] = self.extractor.extract_features(dp['ast'])
                except Exception: # TODO beautify
                    print("failed to retrieve AST/features!")
                    failed_attempts += 1
                    continue
                dps.append(dp)

            # return dps if everything went well
            return dps, po_origin_file

    def contains(self, observation):
        """returns True if given PO formula contains valid ID, features and AST."""
        try:
            if observation['id'] < 0: return False
            if not self.extractor.validate_features(observation['features']): return False
            if not validate_anytree(observation['ast']): return False
        except KeyError:
            return False # a mandatory key is missing
        return True
