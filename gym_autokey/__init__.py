import logging
from gym.envs.registration import register

logger = logging.getLogger(__name__)

register(
    id='autokey-v0',
    entry_point='gym_autokey.envs:AutokeyEnv',
)
