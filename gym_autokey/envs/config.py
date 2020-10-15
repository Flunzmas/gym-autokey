from pathlib import Path

# --- dirs ---

# parent.parent.parent doesn't work
PROJ_ROOT = (Path(__file__).parent / ".." / "..").resolve()
DATA_PATH = PROJ_ROOT / "data"
LOG_PATH = PROJ_ROOT / "logs"
MODELS_PATH = PROJ_ROOT / "models"

# --- data ---

PO_PATH = DATA_PATH / 'po_files'
ALL_PO_FILES = PO_PATH / 'po_all.txt'
TRAIN_PO_FILES = PO_PATH / 'po_all.txt'
TEST_PO_FILES = PO_PATH / 'po_all.txt'
FEATURE_NAMES_FILE = DATA_PATH / 'feature_names.txt'

# --- KeY ---

KEY_PATH = PROJ_ROOT / "key" / "key"
KEY_LOG_PATH = LOG_PATH / "key.log"
KEY_FILES_PATH = KEY_PATH / "key.ui" / "examples"
KEY_TMP_FILE_PATH = "/tmp/"
KEY_TMP_FILE_REGEX = r"^(KeYTmpFileRepo.*|gradle.*)"
KEY_TACTIC_TIME_LIMIT = 18

STOP_KEY_CMD = ['{0}/gradlew'.format(str(KEY_PATH)), '--stop']
START_KEY_SERVER_CMD = "{0}/gradlew server -p {0}".format(str(KEY_PATH))
MAX_FAILED_LOADING_ATTEMPTS = 5

GRADLE_DAEMON_CACHE_DIR = (Path.home() / '.gradle' / 'daemon').as_posix()

# --- socket connections ---

KEY_SERVER_ADDRESS = 'localhost'
KEY_SERVER_PORT = 5533
TACTIC_SERVER_ADDRESS = 'localhost'
TACTIC_SERVER_PORT = 6767

# --- features ---

FEATURE_MODE = "graph"  # options are 'manual', 'graph', 'text_full' and 'text_names'.


# --- hand-picked features ---

OP_CLASSES = [
    "ElementaryUpdate", "Equality", "FormulaSV", "Function",
    "IfExThenElse", "IfThenElse", "Junctor", "LocationVariable",
    "LogicVariable", "Modality", "ModalOperatorSV", "ObserverFunction",
    "ProgramConstant", "ProgramMethod", "ProgramSV", "Quantifier",
    "SchemaVariableFactory", "SkolemTermSV", "SortDependingFunction",
    "TermLabelSV", "TermSV", "Transformer", "UpdateApplication",
    "UpdateJunctor", "UpdateSV", "VariableSV", "WarySubstOp"]

AST_CATEGORIES = {
        # name: (class or name-based, regex to match, alpha-parameter for the
        # counting feature)
        'equalities': ('class', r'^Equality$', 1),
        'inequalities': ('name', r'^(lt|leq|gt|geq)$', 1),  # lt, gt, geq, leq
        'heapStuff': ('name', r'(?i).*(heap).*', 1),  # everything 'heap'
        'anonStuff': ('name', r'(?i).*(anon).*', 1),  # everything 'anon'

        # 1235 etc. and neglit are not necessary to perceive;
        # '#' is there iff Z is there.
        'numbers': ('name', r'^Z$', 1),
        'programConstants': ('class', r'^ProgramConstant$', 1),
        'modalities': ('class', r'^Modality$', 1),
        'if-then-elses': ('class', r'^(IfExThenElse|IfThenElse)$', 1),

        # stuff needed for the usage of dependency contracts
        'dependencyContracts': (
            'class', r'^(ProgramMethod|ObserverFunction)$', 1),
        'quantifiers': ('class', r'^Quantifier$', 1)
}

'''
5 features for each active entry in AST_CATEGORIES, plus:
        overall node count
        overall height feature
        AST formula count (formulas below root)
        select(store(...)) - count
'''
FEATURE_COUNT = len(AST_CATEGORIES) * 5 + 4
FEATURE_DISTRIBUTION_INFORMATION_PATH = (DATA_PATH / 'feature_distribution_information.json').as_posix()

# --- action space ---

TACTIC_ABBR = {
        'INT': 'INT',
        'HEAP': 'HP ',
        'EQUALITY': 'EQ ',
        'AUTO': 'AT ',
        'AUTO_NOSPLIT': 'ATN',
        'MODELSEARCH': 'MS ',
        'SMT': 'SMT',
        'NOTHING': 'NTH',
        'DEPENDENCY': 'DPN',
        'QUANT': 'QNT',
        'EXPAND': 'EXP'
}

TACTICS = ['INT', 'HEAP', 'EQUALITY', 'AUTO', 'AUTO_NOSPLIT', 'MODELSEARCH',
           'SMT', 'NOTHING', 'DEPENDENCY', 'QUANT', 'EXPAND']

# if set to true, the SMT tactic will not be used.
NO_SMT = False

# --- RL ---

POWISE_BUFFER_SIZE = 1000
STEPWISE_BUFFER_SIZE = 10000

# The maximum proving tree depth allowed
ROOT_EPIS_MAX_DEPTH = 20

# After X steps, the current root episode fails by crashing.
MAX_STEPS_PER_PO = 100

# If set to True, whole root episode is discarded if a subepisode fails.
PRE_KILL_FAILED_EPISODES = False
# saves successful episodes for printing at the end of the learning algorithm
REPRINT_SUCCESSFUL_EPISODES = False

# rewards

REWARD_EPISODE_END = 1
PENALTY_EPISODE_END = -1


if __name__ == '__main__':
    print("project root: {}".format(PROJ_ROOT.absolute()))
    print("data path: {}".format(DATA_PATH.absolute()))
