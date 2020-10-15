import torch.nn as nn
from torch.nn.utils.rnn import pack_padded_sequence


class GoalLSTM(nn.Module):
    """
    Credits to yns (https://stackoverflow.com/questions/49832739/variable-size-input-for-lstm-in-pytorch)
    """

    def __init__(self, vocab_size, embed_dim, hidden_dim, num_layers, out_dim):
        super(GoalLSTM, self).__init__()

        self.embedding = nn.Embedding(vocab_size, embedding_dim=embed_dim, padding_idx=0)
        self.dropout = nn.Dropout(0.3)
        self.lstm = nn.LSTM(input_size=embed_dim, hidden_size=hidden_dim, num_layers=num_layers, batch_first=True)
        self.out = nn.Linear(hidden_dim, out_dim)

    def forward(self, x, size):
        x = self.embedding(x)
        x = self.dropout(x)
        x_pack = pack_padded_sequence(x, size, batch_first=True, enforce_sorted=False)
        r_out, (h_n, h_c) = self.lstm(x_pack, None)  # None represents zero initial hidden state
        out = self.out(r_out[:, -1, :])
        return out