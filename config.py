from pathlib import Path

## --- dirs ---

PROJ_ROOT = Path(__file__).parent
DATA_PATH = PROJ_ROOT / "data"
LOG_PATH = PROJ_ROOT / "logs"
MODELS_PATH = PROJ_ROOT / "models"

## --- data ---

PO_PATH = DATA_PATH / 'po_files'
ALL_PO_FILES = PO_PATH / 'po_no_id_all.txt'
TRAIN_PO_FILES = PO_PATH / 'po_no_id_all.txt'
TEST_PO_FILES = PO_PATH / 'po_no_id_all_except_simple_training.txt'
PO_TRAIN_RATIO = 0.8 # fraction of train POs of total POs
FEATURE_NAMES_FILE = DATA_PATH / 'feature_names.txt'

## --- KeY ---

KEY_PATH = PROJ_ROOT / "key"
KEY_LOG_PATH = LOG_PATH / "key.log"
KEY_FILES_PATH = KEY_PATH / "key.ui" / "examples"
KEY_TMP_FILE_PATH = "/tmp/"
KEY_TMP_FILE_REGEX = r"^(KeYTmpFileRepo.*|gradle.*)"
KEY_TACTIC_TIME_LIMIT = 18

STOP_KEY_CMD = ['{0}/gradlew'.format(str(KEY_PATH)), '--stop']
START_KEY_SERVER_CMD = "{0}/gradlew server -p {0}".format(str(KEY_PATH))
MAX_FAILED_LOADING_ATTEMPTS = 5

GRADLE_DAEMON_CACHE_DIR = (Path.home() / '.gradle' / 'daemon').as_posix()

## --- socket connections ---

KEY_SERVER_ADDRESS = 'localhost'
KEY_SERVER_PORT = 5533
TACTIC_SERVER_ADDRESS = 'localhost'
TACTIC_SERVER_PORT = 6767

## --- misc ---

OP_CLASSES = ["ElementaryUpdate", "Equality", "FormulaSV", "Function",
        "IfExThenElse", "IfThenElse", "Junctor", "LocationVariable", "LogicVariable",
        "Modality", "ModalOperatorSV", "ObserverFunction", "ProgramConstant",
        "ProgramMethod", "ProgramSV", "Quantifier", "SchemaVariableFactory",
        "SkolemTermSV", "SortDependingFunction", "TermLabelSV", "TermSV", "Transformer",
        "UpdateApplication", "UpdateJunctor", "UpdateSV", "VariableSV", "WarySubstOp"]

AST_CATEGORIES = {
        # name: (class or name-based, regex to match, alpha-parameter for the counting feature)
        'equalities': ('class', r'^Equality$', 1),
        'inequalities': ('name', r'^(lt|leq|gt|geq)$', 1), # lt, gt, geq, leq
        'heapStuff': ('name', r'(?i).*(heap).*', 1), # everything containing 'heap'
        'anonStuff': ('name', r'(?i).*(anon).*', 1), # everything containing 'anon'
        'numbers': ('name', r'^Z$', 1), # 1235 etc. and neglit are not necessary to perceive, # is there iff Z is there
        'programConstants': ('class', r'^ProgramConstant$', 1),
        'modalities': ('class', r'^Modality$', 1),
        'if-then-elses': ('class', r'^(IfExThenElse|IfThenElse)$', 1),
        'dependencyContracts': ('class', r'^(ProgramMethod|ObserverFunction)$', 1), # stuff needed for the usage of dependency contracts
        'quantifiers': ('class', r'^Quantifier$', 1)
}

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

'''
5 features for each active entry in AST_CATEGORIES, plus:
        overall node count
        overall height feature
        AST formula count (formulas below root)
        select(store(...)) - count
'''
FEATURE_COUNT = len(AST_CATEGORIES) * 5 + 4
FEATURE_DISTRIBUTION_INFORMATION_PATH = (DATA_PATH / 'feature_distribution_information.json').as_posix()

## --- RL ---

LEARNING_STEPS = 240000
POWISE_BUFFER_SIZE = 1000
STEPWISE_BUFFER_SIZE = 10000
ROOT_EPIS_MAX_DEPTH = 20
MAX_STEPS_PER_PO = 100 # After X steps, the current root episode fails by crashing.
PRE_KILL_FAILED_EPISODES = False # If set to True, Algorithm will discard the whole root episode if a subepisode fails.
REPRINT_SUCCESSFUL_EPISODES = False # saves successful episodes for printing at the end of the learning algorithm

# rewards

REWARDER_TYPE = 'sparse' # 'dense'
PENALTY_STEP = -1.0
REWARD_EPISODE_END = 500.0
PENALTY_EPISODE_END = -500.0
PARENT_PROP_GAMMA = 1.0
NON_ROOT_EPIS_FACTOR = 0.0 # Reward is multiplied by this number if the subepisode is not a root episode
