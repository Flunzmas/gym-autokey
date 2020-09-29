import time
import argparse

import numpy as np

import gym_autokey.envs.config as cf

DEFAULT_SPLIT_RATIO = 0.7

def generate_po_files(split_ratio = DEFAULT_SPLIT_RATIO):
    '''
    Randomly splits the PO file containing all POs into two disjoint subsets for training and testing/eval.
    The given ratio determines the relative size of the training subset.
    '''

    if split_ratio < 0.1 or split_ratio > 0.9:
        print("split ratio needs to be between 0.1 and 0.9, using default value.")
        split_ratio = DEFAULT_SPLIT_RATIO

    # retrieve all POs
    with open(cf.ALL_PO_FILES, 'r') as all_po_files:
        po_files = all_po_files.readlines()

    # shuffle and split PO lines
    po_files = np.random.permutation(po_files)
    split_index = int(split_ratio * len(po_files))
    train_pos = po_files[:split_index]
    test_pos = po_files[split_index:]


    # remove newline from last entry
    train_pos[-1] = train_pos[-1][:-1]
    test_pos[-1] = test_pos[-1][:-1]

    # store PO lines
    with open(cf.PO_PATH / ("po_generated_train_" + time.strftime("%d%m%Y-%H%M%S") + ".txt"), 'w') as po_train_file:
        for dp in train_pos:
            po_train_file.write(dp)
    with open(cf.PO_PATH / ("po_generated_test_" + time.strftime("%d%m%Y-%H%M%S") + ".txt"), 'w') as po_test_file:
        for dp in test_pos:
            po_test_file.write(dp)

    print("files were generated.")

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--ratio', type=float, default=DEFAULT_SPLIT_RATIO)
    args = parser.parse_args()

    generate_po_files(args.ratio)