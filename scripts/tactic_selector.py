import time
from numpy import random

import gym_autokey.envs.config as cf
from gym_autokey.envs.feature_extractor.fabric_method import create_feature_extractor
from gym_autokey.envs.datastructures.po_anytree import parse_goal_ast


class TacticSelector:
    """
    This class is used to provide a tactic selector for KeY.
    """

    feature_extractor = None
    tactics = None
    model = None
    selector_hist_path = None
    goal_id_res = None

    last_id = None
    last_features = None
    last_tactic = None

    def __init__(self):
        """
        Instantiates a feature handler as well as a model in order to predict tactics from ASTs.
        """

        self.feature_extractor = create_feature_extractor()

        # --- optional: restrict the tactics that can be applied. -----------------------------------------------

        # TODO
        self.tactics = cf.TACTICS

        # -------------------------------------------------------------------------------------------------------

        # --- optional: load your own learned model. ------------------------------------------------------------

        # TODO

        # -------------------------------------------------------------------------------------------------------

        self.timestamp = time.strftime("%d%m%Y-%H%M%S")
        self.selector_hist_path = (
                cf.LOG_PATH / 'selector_history_{ts}.txt'.format(ts=self.timestamp)).as_posix()
        self.goal_id_res = dict()
        self.reset()

    def predict(self, goal_data):
        """
        returns a tactic from given obligation data by first extracting the features from it.
        """

        # get features and determine tactic per goal ID...
        cur_id = goal_data['id']
        obligation_ast = parse_goal_ast(goal_data)
        cur_features = self.feature_extractor.extract_features(obligation_ast)

        # if a tactic application had no effect, known_goal_id can help to take suited measures.
        known_goal_id = False
        if cur_id == self.last_id and self.feature_extractor.feature_equals(self.last_features, cur_features):
            known_goal_id = True

        # --- optional: use your own learned model to predict a tactic. -----------------------------------------

        chosen_tactic = None
        if self.model is None:

            chosen_tactic = random.choice(self.tactics)
            self.last_id = cur_id
            self.last_features = cur_features
            self.last_tactic = chosen_tactic

        else:
            # TODO
            pass

        # -------------------------------------------------------------------------------------------------------

        # send chosen tactic
        with open(self.selector_hist_path, 'a+') as selector_hist_file:
            selector_hist_file.write('{t} on #{id}\n'.format(t=chosen_tactic, id=cur_id))
        return chosen_tactic

    def reset(self):
        """
        Resets the tactic selector to prepare it for the next PO.
        """
        self.last_id = None
        self.last_features = None
        self.last_tactic = None

        with open(self.selector_hist_path, 'a+') as selector_hist_file:
            selector_hist_file.write('reset\n')
