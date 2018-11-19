import torch
import torch.nn as nn
import torch.optim as optim
from dataset import DataSet
from lstm import LSTMTagger


training_set = DataSet('../dataset/train.utf8')
model = LSTMTagger(3, 3, training_set.vocab_size(), training_set.tagset_size())
loss_function = nn.NLLLoss()
optimizer = optim.SGD(model.parameters(), lr=0.5)


if __name__ == '__main__':
    for epoch in range(300):
        for sentence, tags in training_set:
            model.zero_grad()
            model.hidden = model.init_hidden()

            sentence_in = training_set.prepare_word_sequence(sentence)
            tagsets = training_set.prepare_tag_sequence(tags)

            tag_scores = model(sentence_in)

            loss = loss_function(tag_scores, tagsets)
            loss.backward()
            optimizer.step()

        print('epoch = %d, loss = %f' % (epoch + 1, loss))
    
    with torch.no_grad():
        inputs = training_set.prepare_word_sequence(training_set[0][0])
        tag_scores = model(inputs)
        print(training_set.tag_idxs_to_sequence(torch.argmax(tag_scores, dim=1)))