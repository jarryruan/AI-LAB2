import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
import torch.autograd as autograd

import lstm.utils as utils

class BiLSTM_CRF(nn.Module):
    
    def __init__(self, input_dim, hidden_dim, tag_to_ix, device=torch.device('cpu')):
        super(BiLSTM_CRF, self).__init__()
        self.hidden_dim = hidden_dim
        self.device = device
        self.tag_to_ix = tag_to_ix

        # 增加两个虚拟标签：开始标签、结束标签
        self.END_TAG = '<END_TAG>'
        self.START_TAG = '<START_TAG>'
        self.tag_to_ix[self.END_TAG] = len(self.tag_to_ix)
        self.tag_to_ix[self.START_TAG] = len(self.tag_to_ix)

        self.END_TAG = self.tag_to_ix[self.END_TAG]
        self.START_TAG = self.tag_to_ix[self.START_TAG]

        self.tagset_size = len(self.tag_to_ix)

        # 初始化网络参数
        self.lstm = nn.LSTM(input_dim, hidden_dim // 2, num_layers=1, bidirectional=True)
        self.hidden2tag = nn.Linear(hidden_dim, self.tagset_size)
        self.transitions = nn.Parameter(torch.randn(self.tagset_size, self.tagset_size))
        self.transitions.data[self.START_TAG, :] = -10000
        self.transitions.data[:, self.END_TAG] = -10000
        self.hidden = self.init_hidden()

        self.to(self.device)
    

    def init_hidden(self):
        return (torch.randn(2, 1, self.hidden_dim // 2, device=self.device),
                torch.randn(2, 1, self.hidden_dim // 2, device=self.device))
                
    

    def _get_lstm_features(self, sequence):
        size = len(sequence)
        self.hidden = self.init_hidden()
        embeds = torch.cat(sequence).view(size, 1, -1)
        
        lstm_out, self.hidden = self.lstm(embeds, self.hidden)
        lstm_out = lstm_out.view(size, self.hidden_dim)
        lstm_feats = self.hidden2tag(lstm_out)
        return lstm_feats

    
    def _viterbi_decode(self, feats):
        backpointers = []

        init_vvars = torch.full((1, self.tagset_size), -10000., device=self.device)
        init_vvars[0][self.START_TAG] = 0
        
        forward_vars = init_vvars

        for feat in feats:
            bptrs = []
            viterbivars = []

            for next_tag in range(self.tagset_size):
                next_tag_vars = forward_vars + self.transitions[next_tag]
                best_tag_id = utils.argmax(next_tag_vars)
                bptrs.append(best_tag_id)
                viterbivars.append(next_tag_vars[0][best_tag_id].view(1))
            
            forward_vars = (torch.cat(viterbivars) + feat).view(1, -1)
            backpointers.append(bptrs)

        # 以 <END_TAG> 为结尾
        terminal_var  = forward_vars + self.transitions[self.END_TAG]
        best_tag_id = utils.argmax(terminal_var)
        path_score = terminal_var[0][best_tag_id]

    
        best_path = [best_tag_id]

        for bptrs in reversed(backpointers):
            best_tag_id = bptrs[best_tag_id]
            best_path.append(best_tag_id)
        
        # 以 <START_TAG> 为开头
        start = best_path.pop()
        assert start == self.START_TAG

        best_path.reverse()

        return path_score, best_path


    
    def forward(self, sequence):
        lstm_feats = self._get_lstm_features(sequence)

        score, best_seq = self._viterbi_decode(lstm_feats)
        return score, best_seq


    def loss(self, sequence, tags):
        lstm_feats = self._get_lstm_features(sequence)
        forward_score = self._forward_alg(lstm_feats)
        gold_score = self._score_sentence(lstm_feats, tags)
        return forward_score - gold_score


    def _score_sentence(self, feats, tags):
        score = torch.zeros(1, device=self.device)

        tags = torch.cat([torch.tensor([self.START_TAG], dtype=torch.long, device=self.device), tags])
        
        for (i, feat) in enumerate(feats):
            emit_score = feat[tags[i + 1]]
            trans_score = self.transitions[tags[i + 1], tags[i]]
            score = score + emit_score + trans_score

        score = score + self.transitions[self.END_TAG, tags[-1]]
        return score

    def _forward_alg(self, feats):
        init_alphas = torch.full((1, self.tagset_size), -10000., device=self.device)
        init_alphas[0][self.START_TAG] = 0.
        
        forward_vars = init_alphas
        for feat in feats:
            alphas = []
            for next_tag in range(self.tagset_size):
                emit_score = feat[next_tag].view(1, -1).expand(1, self.tagset_size)
                trans_score = self.transitions[next_tag].view(1, -1)
                next_tag_var = forward_vars + trans_score + emit_score
                alphas.append(utils.log_sum_exp(next_tag_var).view(1))
            
            forward_vars = torch.cat(alphas).view(1, -1)
        
        terminal_var = forward_vars + self.transitions[self.END_TAG]
        return utils.log_sum_exp(terminal_var)