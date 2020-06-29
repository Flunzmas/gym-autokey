import gym
import gym_milkey

env = gym.make('milkey-v0')
env.reset()
for _ in range(1000):
    env.step(env.ac_space.sample()) # take a random action
env.close()