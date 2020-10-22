import torch
import torch.nn as nn
from torch.distributions.categorical import Categorical
import torch.nn.functional as F
import dgl.function as fn
from functools import partial

"""
Taken from https://docs.dgl.ai/en/latest/tutorials/models/1_gnn/4_rgcn.html
"""

class GoalRGCNLayer(nn.Module):
    def __init__(self, in_feat, out_feat, num_rels, num_bases=-1, bias=None,
                 activation=None, is_input_layer=False, w_gain=10):
        super(GoalRGCNLayer, self).__init__()
        self.in_feat = in_feat
        self.out_feat = out_feat
        self.num_rels = num_rels
        self.num_bases = num_bases
        self.bias = bias
        self.activation = activation
        self.is_input_layer = is_input_layer
        self.w_gain = w_gain

        # sanity check
        if self.num_bases <= 0 or self.num_bases > self.num_rels:
            self.num_bases = self.num_rels

        # weight bases in equation (3)
        self.weight = nn.Parameter(torch.Tensor(self.num_bases, self.in_feat,
                                                self.out_feat))
        if self.num_bases < self.num_rels:
            # linear combination coefficients in equation (3)
            self.w_comp = nn.Parameter(torch.Tensor(self.num_rels, self.num_bases))

        # add bias
        if self.bias:
            self.bias = nn.Parameter(torch.Tensor(out_feat))

        # init trainable parameters
        nn.init.xavier_uniform_(self.weight, gain=self.w_gain)
        if self.num_bases < self.num_rels:
            nn.init.xavier_uniform_(self.w_comp, gain=self.w_gain)
        if self.bias:
            nn.init.xavier_uniform_(self.bias, gain=self.w_gain)

    def forward(self, g):
        if self.num_bases < self.num_rels:
            # generate all weights from bases (equation (3))
            weight = self.weight.view(self.in_feat, self.num_bases, self.out_feat)
            weight = torch.matmul(self.w_comp, weight).view(self.num_rels,
                                                            self.in_feat, self.out_feat)
        else:
            weight = self.weight

        if self.is_input_layer:
            def message_func(edges):
                # for input layer, matrix multiply can be converted to be
                # an embedding lookup using source node id

                weight_0 = weight[edges.src['op_class_id'], :, :]
                val = edges.src['op_class_one_hot'].unsqueeze(1)
                msg = torch.bmm(val, weight_0).squeeze()
                msg *= edges.src['norm'].unsqueeze(1)

                return {'msg': msg}
        else:
            def message_func(edges):

                w = weight[edges.src['op_class_id']]
                h = edges.src['h'].unsqueeze(1)
                msg = torch.bmm(h, w).squeeze()
                msg *= edges.src['norm'].unsqueeze(1)

                return {'msg': msg}

        def apply_func(nodes):
            h = nodes.data['h']
            if self.bias:
                h = h + self.bias
            if self.activation:
                h = self.activation(h)
            return {'h': h}

        g.update_all(message_func, fn.sum(msg='msg', out='h'), apply_func)


class GoalRGCN(nn.Module):
    def __init__(self, input_dim, rgcn_h_dim, lin_h_dim, out_dim, rgcn_w_gain, num_rels,
                 num_bases=-1, num_hidden_layers=1):
        super(GoalRGCN, self).__init__()
        self.input_dim = input_dim
        self.rgcn_h_dim = rgcn_h_dim
        self.lin_h_dim = lin_h_dim
        self.out_dim = out_dim
        self.rgcn_w_gain = rgcn_w_gain
        self.num_rels = num_rels
        self.num_bases = num_bases
        self.num_hidden_layers = num_hidden_layers

        # create rgcn layers
        self.build_model()

    def build_model(self):
        self.layers = nn.ModuleList()
        # input to hidden
        i2h = self.build_input_layer()
        self.layers.append(i2h)
        # hidden to hidden
        for _ in range(self.num_hidden_layers):
            h2h = self.build_hidden_layer()
            self.layers.append(h2h)
        # hidden to output
        h2o = self.build_output_layer()
        self.layers.append(h2o)

        # build actor and critic
        self.build_actor()
        self.build_critic()

    def build_actor(self):
        self.actor = nn.Sequential(
            nn.Linear(self.rgcn_h_dim, self.lin_h_dim),
            nn.ReLU(),
            nn.Linear(self.lin_h_dim, self.out_dim)
        )

    def build_critic(self):
        self.critic = nn.Sequential(
            nn.Linear(self.rgcn_h_dim, self.lin_h_dim),
            nn.ReLU(),
            nn.Linear(self.lin_h_dim, 1)
        )

    def build_input_layer(self):
        return GoalRGCNLayer(self.input_dim, self.rgcn_h_dim, self.num_rels, self.num_bases,
                             activation=F.relu, is_input_layer=True, w_gain=self.rgcn_w_gain)

    def build_hidden_layer(self):
        return GoalRGCNLayer(self.rgcn_h_dim, self.rgcn_h_dim, self.num_rels, self.num_bases,
                             activation=F.relu, w_gain=self.rgcn_w_gain)

    def build_output_layer(self):
        return GoalRGCNLayer(self.rgcn_h_dim, self.out_dim, self.num_rels, self.num_bases,
                             activation=partial(F.softmax, dim=1), w_gain=self.rgcn_w_gain)

    def forward(self, g):
        for layer in self.layers:
            layer(g)

        hidden_outs = g.ndata.pop('h')
        root_node_features = hidden_outs[0]
        probs = self.actor(root_node_features)
        dist = Categorical(probs)
        value = self.critic(root_node_features)
        return dist, value
