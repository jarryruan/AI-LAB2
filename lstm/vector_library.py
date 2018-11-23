import torch


class VectorLibrary:
    def __init__(self, filename, encoding='utf8'):
        self.data = dict()
        with open(filename, 'r', encoding=encoding) as f:
            rows = f.readlines()
            self.vocab_size , self.vector_dim = tuple(map(int, rows[0].split()))
            for row in rows[1:]:
                arr = row.split()
                word = arr[0]
                vector = torch.tensor(list(map(float, arr[1:])))
                self.data[word] = vector
    
    def __getitem__(self, key):
        if(key in self.data.keys()):
            return self.data[key]
        else:
            return torch.randn(self.vector_dim)
    
    def __len__(self):
        return len(self.data)