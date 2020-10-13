import re
import os
import json
from math import sqrt, tanh
from statistics import mean

import numpy as np

import gym_autokey.envs.config as cf
from gym_autokey.envs.datastructures.po_anytree import ast_anytree_to_node_list
import gym_autokey.envs.feat_extractor.feat_extractor as fe

class ManualFeatExtractor(fe.FeatExtractor):
    """
    This class extracts features from ASTs.
    """

    feature_distribution_information = dict()
    feature_count = cf.FEATURE_COUNT

    def __init__(self, unnormalized_features=False):
        """
        Initializes a feature extractor by loading all available features.
        """
        self.unnormalized_features = unnormalized_features
        self._load_feature_distribution_information()

    def _load_feature_distribution_information(self):
        """
        loads the distribution information for all feature categories which have to be normalized.
        The normalization uses these values in an application of the tanh() function.
        """
        if not os.path.exists(cf.FEATURE_DISTRIBUTION_INFORMATION_PATH):
            raise FileNotFoundError('there is no file containing the distribution information for the categories!')
        with open(cf.FEATURE_DISTRIBUTION_INFORMATION_PATH, 'r') as infile:
            self.feature_distribution_information = json.load(infile)

    def get_feature_defaults(self):
        """
        returns a list containing neutral values for all features.
        """
        return [0.0 for i in range(self.feature_count)]

    # ----------------------------------------------------------------------------------------

    @staticmethod
    def get_fitting_nodes(node_list, ast_category):
        """
        Returns those nodes from the AST that correspond to given feature category.
        """
        name_or_class = ast_category[0]
        cat_regex = ast_category[1]

        if name_or_class == 'class':
            return [node for node in node_list if re.match(cat_regex, node.op_class) is not None]
        else:
            return [node for node in node_list if re.match(cat_regex, node.op_name) is not None]

    def tanh_normalize(self, category, value):
        """
        A tanh-based normalization function that depends on the feature category given.
        """
        offset = self.feature_distribution_information[category + '_mean']
        sigma = self.feature_distribution_information[category + '_sigma']
        if sigma == 0.0:
            return -1.0 if value < offset else 1.0
        return tanh((value - offset) / (sigma * 2))

    def abs_node_count_feature(self, nodes):
        """
        A feature with higher values for ASTs with more nodes.
        """
        abs_node_count = len(nodes)
        if not self.unnormalized_features:
            abs_node_count = self.tanh_normalize('overall_node_count', abs_node_count)
        return abs_node_count

    def abs_height_feature(self, nodes):
        """
        A feature corresponding to the general height of the whole AST.
        Lower values mean a wider tree, higher values a taller one.
        """
        depth_sum = sum([node.depth for node in nodes])
        n = len(nodes)
        n_minus_2_stair_sum = (n - 2) * (n - 1) / 2  # sum of 1, 2 ..., n-2.
        abs_height = 2 * ((depth_sum - (n - 1)) / n_minus_2_stair_sum) - 1
        if not self.unnormalized_features:
            abs_height = self.tanh_normalize('overall_height', abs_height)
        return abs_height

    def abs_formula_count_feature(self, root_node):
        """
        A feature counting the total number of formulas that are children of root.
        """
        formula_counts = [len(ast_part.children) for ast_part in root_node.children]
        abs_formula_count = sum(formula_counts)
        if not self.unnormalized_features:
            abs_formula_count = self.tanh_normalize('overall_formula_count', abs_formula_count)
        return abs_formula_count

    @staticmethod
    def present_feature(nodes):
        """
        -1.0 if no nodes present, else 1.0.
        """
        if len(nodes) <= 0:
            return -1.0
        else:
            return 1.0

    @staticmethod
    def balance_feature(cat_nodes, all_nodes):
        """
        Determines the balance of nodes of given cat. compared to the general AST.
        negative: more present towards the left side of the AST.
        positive: more present towards the right side of the AST.
        """
        cat_ids = [node.id for node in cat_nodes]
        relative_node_ids = [2 * node.id / len(all_nodes) for node in cat_nodes]
        balance = sum([id_value - 1 for id_value in relative_node_ids]) / len(cat_ids)
        return balance

    @staticmethod
    def spread_feature(cat_nodes):
        """
        Determines the spread of node of given cat. over the whole AST.
        -1.0: no id spread in cat nodes
        1.0: maximum id spread in cat nodes
        """
        cat_ids = [node.id for node in cat_nodes]
        cat_max = max(cat_ids)
        cat_min = min(cat_ids)
        if cat_min == cat_max:
            return -1.0  # no variance

        cat_mu = mean(cat_ids)
        std_deviation = sqrt(sum([(id - cat_mu) ** 2 for id in cat_ids]) / len(cat_ids))
        m1_to_1_normed_deviation = 4 * (std_deviation / (max(cat_ids) - min(cat_ids))) - 1
        return m1_to_1_normed_deviation

    def rel_count_feature(self, category, cat_nodes, all_nodes):
        """
        A feature yielding the relative frequency of nodes of given cat.
        """
        rel_count = len(cat_nodes) / len(all_nodes)
        if not self.unnormalized_features:
            rel_count = self.tanh_normalize(category + '_rel_count', rel_count)
        return rel_count

    def rel_depth_feature(self, category, cat_nodes, all_nodes):
        """
        A feature for the relative depth in which nodes of given cat. occur.
        low: low depth (close to root), high: high depth (deep into the tree)
        """
        all_node_depths = [node.depth for node in all_nodes]
        cat_node_depths = [node.depth for node in cat_nodes]
        rel_depth = mean(cat_node_depths) / max(all_node_depths)
        if not self.unnormalized_features:
            rel_depth = self.tanh_normalize(category + '_rel_depth', rel_depth)
        return rel_depth

    def tree_select_of_store_count_feature(self, all_nodes):
        """
        A feature for occurrences of select(store(...)).
        Low: few such occurrences, high: a lot of them.
        """
        select_nodes = [node for node in all_nodes if node.op_name.endswith('::select')]
        select_of_store_occurrences = 0
        for select_node in select_nodes:
            select_of_store_occurrences += [child.op_name for child in select_node.children].count('store')
        if not self.unnormalized_features:
            select_of_store_occurrences = self.tanh_normalize('tree_select_of_store_count', select_of_store_occurrences)
        return select_of_store_occurrences

    # ----------------------------------------------------------------------------------------

    def extract_features(self, goal_ast):
        """
        Extracts pre-defined features from the given obligation ast.
        """
        features = dict()
        all_nodes = ast_anytree_to_node_list(goal_ast)
        for cat in cf.AST_CATEGORIES:
            cat_nodes = self.get_fitting_nodes(all_nodes, cf.AST_CATEGORIES[cat])
            if len(cat_nodes) == 0:  # insert (filterable dummy|neutral) values (if|if not) in feature analysis mode.
                features[cat + '_present'] = -1.0
                features[cat + '_balance'] = -2.0 if self.unnormalized_features else 0.0
                features[cat + '_spread'] = -2.0 if self.unnormalized_features else 0.0
                features[cat + '_rel_count'] = -2.0 if self.unnormalized_features else 0.0
                features[cat + '_rel_depth'] = -2.0 if self.unnormalized_features else 0.0
            else:
                features[cat + '_present'] = 1.0
                features[cat + '_balance'] = self.balance_feature(cat_nodes, all_nodes)
                features[cat + '_spread'] = self.spread_feature(cat_nodes)
                features[cat + '_rel_count'] = self.rel_count_feature(cat, cat_nodes, all_nodes)
                features[cat + '_rel_depth'] = self.rel_depth_feature(cat, cat_nodes, all_nodes)

        features['overall_node_count'] = self.abs_node_count_feature(all_nodes)
        features['overall_height'] = self.abs_height_feature(all_nodes)
        features['overall_formula_count'] = self.abs_formula_count_feature(goal_ast)
        features['tree_select_of_store_count'] = self.tree_select_of_store_count_feature(all_nodes)

        return features

    def analyze_feature_distributions(self, all_features):
        """
        Given a list of feature dicts, assembles distribution information
        and prints it to the console. If in feature analysis mode, dist. info is written to a file
        for those features that need to be tanh-normalized.
        """
        distribution_information = dict()
        print('')
        print('total: {n} features.'.format(n=self.feature_count))
        for key in all_features[0]:

            cur_feature_values = [f_dict[key] for f_dict in all_features if f_dict[key] >= -1.0]
            if not cur_feature_values:
                print('no value found for {f}, setting to 0...'.format(f=key))
                cur_feature_values = [0.0]

            cur_mean = mean(cur_feature_values)
            cur_median = np.percentile(cur_feature_values, 50)
            cur_sigma = np.std(cur_feature_values)

            for p in [0, 25, 50, 75, 100]:
                print('{key} {p}-percentile: {val}'.format(key=key, p=p, val=np.percentile(cur_feature_values, p)))

            print('{key} mean: {val}'.format(key=key, val=cur_mean))
            # print('{key} median: {val}'.format(key=key, val = cur_median))
            print('{key} std: {val}'.format(key=key, val=cur_sigma))
            print('')

            # these ones are inherently normalized and don't need to be further considered.
            if ('balance' in key) or ('present' in key) or ('spread' in key):
                continue

            distribution_information[key + '_mean'] = cur_mean
            distribution_information[key + '_median'] = cur_median
            distribution_information[key + '_sigma'] = cur_sigma

        if self.unnormalized_features:
            with open(cf.FEATURE_DISTRIBUTION_INFORMATION_PATH, 'w') as outfile:
                outfile.write(json.dumps(distribution_information))

    # ----------------------------------------------------------------------------------------

    @staticmethod
    def compare_features(features_1, features_2):
        """
        Returns the edit distance between two feature vectors.
        """

        if len(features_1) != len(features_2):
            raise AttributeError("feature lists don't contain the same number of features!")
        squared_dist = 0.0
        for i in range(features_1):
            squared_dist += (features_1[i] - features_2[i]) ** 2
        return sqrt(squared_dist)

    def feature_equals(self, features_1, features_2):
        """
        Returns true if two feature vectors are quasi-equal, false otherwise.
        """
        return self.compare_features(features_1, features_2) < 0.0001

    @staticmethod
    def validate_features(features):
        """
        Validates each given feature and returns True only if all are valid.
        """
        for key in features:
            if abs(features[key]) > 1.0:
                return False
        return True