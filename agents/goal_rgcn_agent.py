import time

import numpy as np
from operator import itemgetter

import torch
import torch.optim as optim

import gym
from gym_autokey.envs.obs_extractor.graph_obs_extractor import op_class_count
from models.goal_rgcn import GoalRGCN

""" Code inspiration: https://github.com/colinskow/move37/blob/master/ppo/ppo_train.py """

# -- tunable hyperparams (non-tunables are initialized in train()) -------------

rgcn_w_gain         = 10
rgcn_h_dim          = 60
lin_h_dim           = 120

learning_rate       = 1e-4
gamma               = 0.99
gae_lambda          = 0.95
ppo_clip            = 0.2
critic_discount     = 0.5
entropy_beta        = 0.001

ppo_steps           = 3232
mini_batch_size     = 202
ppo_epochs          = 10

min_po_attempts     = 1000
target_po_ratio     = 0.95
render_each_step    = False

# ------------------------------------------------------------------------------

def compute_gae(next_value, rewards, masks, values, gamma=gamma, lam=gae_lambda):
    values = values + [next_value]
    gae = 0
    returns = []
    for step in reversed(range(len(rewards))):
        delta = rewards[step] + gamma * values[step + 1] * masks[step] - values[step]
        gae = delta + gamma * lam * masks[step] * gae
        # prepend to get correct order back
        returns.insert(0, gae + values[step])
    return returns

def normalize(x):
    x -= x.mean()
    x /= (x.std() + 1e-8)
    return x


def ppo_iter(states, actions, log_probs, returns, advantage):
    batch_size = len(states)
    # generates random mini-batches until we have covered the full batch
    for _ in range(batch_size // mini_batch_size):
        rand_ids = np.random.randint(0, batch_size, mini_batch_size)
        picked_actions = actions[rand_ids]
        picked_log_probs = log_probs[rand_ids]
        picked_returns = returns[rand_ids]
        picked_advantage = advantage[rand_ids]
        yield list(itemgetter(*rand_ids)(states)), picked_actions , picked_log_probs, picked_returns, picked_advantage

def ppo_update(model, optimizer, frame_idx, states, actions, log_probs, returns, advantages, clip_param=ppo_clip):

    # PPO EPOCHS is the number of times we will go through ALL the training data to make updates
    for _ in range(ppo_epochs):
        # grabs random mini-batches several times until we have covered all data
        for s_mb, a_mb, olp_mb, rt_mb, av_mb in ppo_iter(states, actions, log_probs, returns, advantages):

            losses = []
            for state, action, old_log_probs, return_, advantage in zip(s_mb, a_mb, olp_mb, rt_mb, av_mb):

                dist, value = model(state)
                entropy = dist.entropy().mean()
                new_log_probs = dist.log_prob(action)

                ratio = (new_log_probs - old_log_probs).exp()
                surr1 = ratio * advantage
                surr2 = torch.clamp(ratio, 1.0 - clip_param, 1.0 + clip_param) * advantage

                actor_loss = - torch.min(surr1, surr2).mean()
                critic_loss = (return_ - value).pow(2).mean()

                loss = critic_discount * critic_loss + actor_loss - entropy_beta * entropy
                losses.append(loss)

            optimizer.zero_grad()
            torch.stack(losses).mean().backward()
            optimizer.step()

def train():

    # ------ model and env ------

    env = gym.make('autokey-v0', self_render=False)
    state = env.reset()

    num_op_classes = op_class_count  # number of op_classes
    num_tactics = len(env.connector.available_tactics)

    model = GoalRGCN(input_dim=num_op_classes, rgcn_h_dim=rgcn_h_dim, lin_h_dim=lin_h_dim, out_dim=num_tactics,
                     rgcn_w_gain=rgcn_w_gain, num_rels=num_op_classes,
                     num_bases=-1, num_hidden_layers=2)

    optimizer = optim.Adam(model.parameters(), lr=learning_rate)

    # ------ vars etc. ------

    frame_idx = 0
    iteration = 0

    # ------ TRAINING LOOP ------

    while True:

        iter_start = time.time()
        log_probs = []
        values = []
        states = []
        actions = []
        rewards = []
        masks = []

        for i in range(ppo_steps):

            dist, value = model(state)
            action = dist.sample()
            next_state, reward, done, _ = env.step(action)
            if render_each_step:
                env.render()
            log_prob = dist.log_prob(action)

            log_probs.append(log_prob)
            values.append(value)
            rewards.append(torch.tensor(reward).unsqueeze(0))
            masks.append(torch.tensor(1 - done).unsqueeze(0))
            states.append(state)
            actions.append(action)

            state = next_state
            frame_idx += 1

        _, next_value = model(next_state)
        returns = compute_gae(next_value, rewards, masks, values)

        returns = torch.FloatTensor(returns).detach()
        log_probs = torch.FloatTensor(log_probs).detach()
        values = torch.FloatTensor(values).detach()
        actions = torch.FloatTensor(actions)
        advantage = returns - values
        advantage = normalize(advantage)

        ppo_update(model, optimizer, frame_idx, states, actions, log_probs, returns, advantage)

        iter_stop = time.time()
        print("\nPPO iteration {0} done in {1} secs. Env stats:".format(iteration, round(iter_stop - iter_start, 2)))
        env.render(mode='cli_basic')

        if can_stop_training(env):
            print("Training stop condition met, finishing training.")
            break
        else:
            iteration += 1

    # ------ cleanup ------

    env.close()


def can_stop_training(env):
    return len(env.po_success_history) > min_po_attempts and env.po_percent > target_po_ratio


if __name__ == "__main__":
    train()