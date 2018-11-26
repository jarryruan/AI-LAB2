import torch
import torch.nn as nn
import torch.optim as optim

from lstm.dataset import DataSet
from lstm.lstm import BiLSTM_CRF
from lstm.vector_library import VectorLibrary

# 用测试集计算当前模型的误差
def accuracy(model, test_set):
    n = len(test_set)
    correct = 0
    total = 0
    for (i, (sentence, tags)) in enumerate(test_set):
        if(i % 1000 == 0):
            print('complete: %f%%' % (i / n * 100))
        sentence_in = test_set.prepare_word_sequence(sentence)
        tagsets = torch.tensor(test_set.prepare_tag_sequence(tags))
        _, outputs = model(sentence_in)
        outputs = torch.tensor(outputs)

        correct = correct + torch.sum(tagsets == outputs).item()
        total = total + len(sentence)
    
    return (correct / total)


# 输出模型对当前测试集的输出
def outputs(model, test_set, output_path):
    s = '\n'
    n = len(test_set)
    with open(output_path, 'w') as f:
        for (i, (sentence, tags)) in enumerate(test_set):
            if(i % 1000 == 0):
                print('complete: %f%%' % (i / n * 100))
            sentence_in = test_set.prepare_word_sequence(sentence)
            _, tagsets = model(sentence_in)
            tagsets = test_set.tag_idxs_to_sequence(tagsets)
            f.write('%s%s%s' % (s.join(tagsets), s, s))


# 初始化模型，返回模型对象与训练集对象
def init_model(vector_library_path, training_set_path, enable_gpu):
    device = torch.device("cuda:0" if (torch.cuda.is_available() and enable_gpu) else 'cpu')

    print("loading vector library...")
    vl = VectorLibrary(vector_library_path, device=device)

    print("loading training set...")
    training_set = DataSet(training_set_path, vl)

    # 创建 BiLSTM-CRF 网络，并在可行的情况下使用GPU加速
    model = BiLSTM_CRF(vl.vector_dim, 100, training_set.tag_to_ix, device)
    if (torch.cuda.is_available() and enable_gpu):
        model = model.cuda()
    
    return model, training_set





def train(model, training_set, epoch, enable_gpu):
    if(torch.cuda.is_available() and enable_gpu):
        torch.backends.cudnn.benchmark = True

    device = torch.device("cuda:0" if (torch.cuda.is_available() and enable_gpu) else 'cpu')
    print('working device = %s' % str(device))

    optimizer = optim.SGD(model.parameters(), lr=0.01, weight_decay=1e-4)
    n = len(training_set)

    print("start training...")
    for e in range(epoch):
        for (i, (sentence, tags)) in enumerate(training_set):

            if(i % 100 == 0):
                print('epoch = %d, progress = %f%%' % (e + 1 ,i / n * 100))

            model.zero_grad()

            sentence_in = training_set.prepare_word_sequence(sentence)
            tagsets = training_set.prepare_tag_sequence(tags)

            loss = model.loss(sentence_in, tagsets)

            loss.backward()
            optimizer.step()

        print('epoch = %d complete, loss = %f' % (e + 1, loss.item()))