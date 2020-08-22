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

TODO
