# Automated deductive Program verification in KeY

This is a custom OpenAI gym environment for automated deductive program verification in KeY.

# Background

TODO

# Installation

The procedure has been tested with python 3.8.


1. Install the gym environment
```
git clone git@github.com:Flunzmas/gym-autokey.git
cd gym-autokey
pip install -e .
```

2. Install the included version of KeY and prepare the corresponding proof files.
```
cd key/key/
./gradlew :key.ui:installDist
rm -r key.ui/examples/
cp -r ../../autokey_examples/ key.ui/examples/
```

3. Optional: install the Z3 SMT solver and add its bin directory to your PATH (If this is skipped, applying the "SMT" tactic to a goal has no effect).

# Training

TODO

# Testing/Evaluation

In order to evaluate a trained tactic selection model, the given fork of KeY includes an _AIServerMacro_ that promts KeY to query for the next tactic to apply instead of using its own auto mode. By starting a dedicated tactic server that accepts messages containing goal ASTs and that responds with a tactic command, you provide KeY with the tactics that lead it to a proof for given PO!

1. Start the tactic server by executing `tactic_server.py`. It creates a _TacticSelector_ that loads your trained model and uses it to predict tactics given the forwarded goal ASTs (Communication between tactic server and KeY is realized using a socket connection on port 6767, see `gym-autokey/envs/config.py`).

2. Evaluate your model, optionally pitting it against KeY's built-in auto mode, by executing `evaluate.py <po_file>`. Replace `<po_file>` with the name of any of the PO files (see `data/po_files/name_explanation.md` for an explanation of what the different po files offer.)
