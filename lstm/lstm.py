import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim

class LSTMTagger(nn.Module):
    
    def __init__(self, input_dim, hidden_dim, vocab_size, tagset_size):
        super(LSTMTagger, self).__init__()
        self.hidden_dim = hidden_dim
        self.lstm = nn.LSTM(input_dim, hidden_dim)

        self.hidden2tag = nn.Linear(hidden_dim, tagset_size)
        self.hidden = self.init_hidden()
    
    def init_hidden(self):
        return (torch.zeros(1, 1, self.hidden_dim),
                torch.zeros(1, 1, self.hidden_dim))
                
    
    def forward(self, sequence):
        size = len(sequence)
        embeds = torch.cat(sequence)
        
        lstm_out, self.hidden = self.lstm(embeds.view(size, 1, -1), self.hidden)
        tag_space = self.hidden2tag(lstm_out.view(size, -1))
        tag_scores = F.log_softmax(tag_space, dim=1)
        return tag_scores