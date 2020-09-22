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

In order to evaluate a trained tactic selection model, the given fork of KeY includes an _AIServerMacro_ that promts KeY to query for the next tactic to apply instead of using its own auto mode. By starting a dedicated _TacticServer_ (defined in `tactic_server.py`) that accepts messages containing goal ASTs and that responds with a tactic command, you provide KeY with the tactics that lead it to a proof for given PO.

The _TacticSelector_ defined in `tactic_selector.py` provides a class wrapper including the function `predict()`. This function is called by the _TacticServer_ and is given an observation, by default returning a random tactic. However, by inputting your code for accessing your model you can use the model to return predictions to KeY.

1. Edit `tactic_selector.py` and fill in the TODO-ed sections to use your learned model for the predictions. If you want to use a random selector, just leave the file as-is.

2. Start the tactic server by executing `tactic_server.py`. It creates the _TacticSelector_ that loads your trained model and uses it to predict tactics given the forwarded goal ASTs (Communication between tactic server and KeY is realized using a socket connection on port 6767, see `gym-autokey/envs/config.py`).

3. Evaluate your model, optionally pitting it against KeY's built-in auto mode, by executing `evaluate.py <po_file>`. Replace `<po_file>` with the name of any of the PO files (see `data/po_files/name_explanation.md` for an explanation of what the different po files offer). While a performance overview is printed to the terminal, the TacticSelector saves a tactic selection history as a .txt file to your log folder.
