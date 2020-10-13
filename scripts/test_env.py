import gym
import gym_autokey

env = gym.make('autokey-v0', self_render=False)
env.reset()
for _ in range(220):
    obs, rew, done, _ = env.step(env.ac_space.sample())  # take a random action
    env.render()
env.close()
