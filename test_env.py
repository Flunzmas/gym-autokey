import gym
import gym_milkey

env = gym.make('milkey-v0', self_render = False)
env.reset()
for _ in range(1000):
    obs, rew, done, _ = env.step(env.action_space.sample()) # take a random action
    env.render()
env.close()