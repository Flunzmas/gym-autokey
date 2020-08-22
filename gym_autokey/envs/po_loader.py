import random

import gym_autokey.envs.config as cf
from gym_autokey.envs.datastructures.ck_status_error import CKStatusError


def load_data_point(kc):
    """
    Loads a PO data point by loading the corresponding proof file into KeY.
    Returns the selected PO ID and the file path. 
    """

    with open(cf.TRAIN_PO_FILES, 'r') as po_file:
        po_origin_files = [line.rstrip('\n') for line in po_file]
        if not po_origin_files[0][0].isdigit():  # load the whole PO file if no indices are given.
            selected_po_file = random.choice(list(set(po_origin_files)))
        else:  # we don't need the po_index.
            po_origin_files = [line.rstrip('\n').split(maxsplit=1)[1] for line in po_origin_files]
            selected_po_file = random.choice(list(set(po_origin_files)))

    selected_goal_ids = []

    try:  # to load the selected file into KeY and fetch the goal IDs.
        loaded_goal_ids = kc.load_file(selected_po_file)
        selected_goal_ids.extend(loaded_goal_ids)

    except (IOError, AttributeError, CKStatusError, IndexError) as e:
        print("Could not load from {0} ({1})".format(str(selected_po_file), e))
    return selected_goal_ids, selected_po_file
