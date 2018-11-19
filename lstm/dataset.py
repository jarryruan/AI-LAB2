import torch

class DataSet:
    def __init__(self, filename, encoding='utf8'):
        self.word_to_ix = {}
        self.tag_to_ix = {}

        self.data = []
        file = open(filename, 'r', encoding=encoding)
        self.load_from_file(file)
        file.close()

        self.ix_to_tag = dict((v, k) for k,v in self.tag_to_ix.items())

    
    def load_from_file(self, file):
        lines = file.readlines()
        sentence = ([], [])
        for i in lines:
            i = i.strip()
            if(len(i) > 0):
                word, tag = i.split()
                sentence[0].append(word)
                sentence[1].append(tag)

                if(word not in self.word_to_ix):
                    self.word_to_ix[word] = len(self.word_to_ix)

                if(tag not in self.tag_to_ix):
                    self.tag_to_ix[tag] = len(self.tag_to_ix)

            else:
                self.data.append(sentence)
                sentence = ([], [])

        self.data.append(sentence)



    def prepare_word_sequence(self, seq):
        idxs = [self.word_to_ix[w] for w in seq]
        return torch.tensor(idxs, dtype=torch.long)
    
    def prepare_tag_sequence(self, seq):
        idxs = [self.tag_to_ix[w] for w in seq]
        return torch.tensor(idxs, dtype=torch.long)
    

    def tag_idxs_to_sequence(self, idxs):
        return [self.ix_to_tag[ix.item()] for ix in idxs]

    
    def vocab_size(self):
        return len(self.word_to_ix)
        
    
    def tagset_size(self):
        return len(self.tag_to_ix)


    def __getitem__(self, key):
        return self.data[key]
    

    def __len__(self):
        return len(self.data)
    





