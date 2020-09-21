import subprocess
import sys

import numpy as np

import gym_autokey.envs.config as cf


def evaluate(po_filepath: str, time_limit: int = 60, ai_only: bool = False):
    """
    Evaluate the learned auto mode against the POs taken from given po file.
    :param po_filepath: The path to the PO file you want to use to get the POs to evaluate from.
    :param time_limit: Time limit in seconds.
    :param ai_only: If true, skips evaluation of KeY's auto mode on given PO files.
    """

    # the common base for both evaluation modes: The one running KeY's builtin auto mode
    # and the one running the learned and saved tactic selector.
    key_eval = 'timeout -s SIGKILL {tl} {kp}/scripts/key'.format(tl=time_limit, kp=cf.KEY_PATH.as_posix())

    # The suffix determining to run KeY's builtin auto mode (filepath to be inserted on-demand)
    key_eval_std_cmd = '--auto \'{0}\''  #

    # The suffix determining to run the learned and saved tactic selector (filepath to be inserted on-demand)
    key_eval_ai_cmd = '--auto --macro AIServerMacro \'{0}\''

    po_filepaths = []
    po_filepath = cf.DATA_PATH / po_filepath
    with open(po_filepath, 'r') as test_po_file:
        test_dps = [line.rstrip('\n') for line in test_po_file]
    if not test_dps[0][0].isdigit():
        po_filepaths.extend(set(test_dps))
    else:
        po_filepaths.extend(set([dp.split(maxsplit=1)[1] for dp in test_dps]))
    po_filepaths = np.random.permutation(po_filepaths)  # randomly permutate the evaluation files

    success_codes_std = []
    success_codes_ai = []

    for po_fp in po_filepaths:
        po_fp = (cf.KEY_FILES_PATH / po_fp).as_posix()

        if not ai_only:
            # evaluate KeY's built-in auto mode
            std_cmd = key_eval.split()
            std_cmd.append(key_eval_std_cmd.format(po_fp))
            cur_code = run_single_file(std_cmd)
            print("STD returned {0}".format(cur_code))
            success_codes_std.append(cur_code)

        # evaluate the trained auto mode
        ai_cmd = key_eval.split()
        ai_cmd.append(key_eval_ai_cmd.format(po_fp))
        cur_code = run_single_file(ai_cmd)
        print('AI returned {0}'.format(cur_code))
        success_codes_ai.append(cur_code)

    print("\n\ntotal: STD [{0}/{1}], AI [{2}/{3}]".format(success_codes_std.count(0), len(success_codes_std),
                                                          success_codes_ai.count(0), len(success_codes_ai)))
    print('results for each file:')
    for i in range(len(po_filepaths)):
        if i < len(success_codes_std):
            print('{0}: STD {1}, AI {2}'.format(po_filepaths[i], success_codes_std[i], success_codes_ai[i]))
        else:
            print('{0}: STD {1}, AI {2}'.format(po_filepaths[i], '-', success_codes_ai[i]))


def run_single_file(cmd):
    """
    Runs a single evaluation process for a single file
    """
    cmd = ' '.join(cmd)
    print('running {cmd}...'.format(cmd=cmd))
    return subprocess.call(cmd, shell=True)


#  =============================================

if __name__ == "__main__":
    if len(sys.argv) == 2:
        evaluate(po_filepath=sys.argv[1])
    elif len(sys.argv) == 3:
        evaluate(po_filepath=sys.argv[1], time_limit=int(sys.argv[2]))
    else:
        evaluate(po_filepath=sys.argv[1], time_limit=int(sys.argv[2]),
                 ai_only=(sys.argv[3] == "True" or int(sys.argv[3]) != 0))
