import gym
import gym_autokey
from gym_autokey.envs.obs_extractor.graph_obs_extractor import op_class_count

from models.goal_rgcn import GoalRGCN

env = gym.make('autokey-v0', self_render=False)
obs = env.reset()

num_op_classes = op_class_count # number of op_classes
hidden_dim = 120 # tunable
num_tactics = len(env.connector.available_tactics)

model = GoalRGCN(input_dim=num_op_classes, h_dim=hidden_dim, out_dim=num_tactics, num_rels=num_op_classes,
                 num_bases=-1, num_hidden_layers=1)

for _ in range(1000):

    softmax_out = model.forward(obs)
    exit(1)
    obs, rew, done, _ = env.step(env.ac_space.sample())  # take a random action
    env.render()
env.close()
