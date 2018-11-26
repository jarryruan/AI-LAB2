import torch
import process
from lstm.dataset import DataSet
from lstm.vector_library import VectorLibrary

# 全局初始化
torch.manual_seed(1)


#是否尝试开启GPU加速
enable_gpu = False


# 从标准输入读入字符串
def get_input_str(tip, default_value):
    x = str(input('%s (default=%s):' % (tip, default_value)))
    if(len(x.strip()) == 0):
        return default_value
    return x

# 从标准输入读入整数
def get_input_int(tip, default_value):
    x = input('%s (default=%s):' % (tip, default_value))
    if(not x.strip()):
        return default_value

    try:
        return int(x)
    except ValueError:
        return get_input_int(tip, default_value)


if __name__ == '__main__':
    print('(1) Train New BiLSTM+CRF Model')
    print('(2) Load Exist Model')
    print('(3) Exit\n')

    task = get_input_int('select your task', 1)

    if(task == 1):
        vl = get_input_str('enter vector library path', 'vector_library.utf8')
        ts = get_input_str('enter training set path', 'train.utf8')
        epoch = get_input_int('enter epoch number', 1)
        save = get_input_str('enter model saving path', 'model.lstm')
        model, training_set = process.init_model(vl, ts, enable_gpu)
        process.train(model, training_set, epoch, enable_gpu)
        print('saving your model...')
        torch.save(model, save)

    elif(task == 2):
        md = get_input_str('enter your model path', 'model.lstm')
        vl = get_input_str('enter vector library path', 'vector_library.utf8')
        print('loading vector library...')
        vector_library = VectorLibrary(vl)

        print('loading model...')
        model = torch.load(md)
        model.eval()

        while(True):
            print('(1) Test Testset Accuracy')
            print('(2) Calculate Sequence Output')
            print('(3) Go on training')
            print('(4) Exit\n')

            task = get_input_int('select your task', 1)

            if(task == 1):
                ts = get_input_str('enter test set path', 'test.utf8')
                print('loading testset...')
                test_set = DataSet(ts, vector_library)
                print('calculating accuracy...')
                print('accuracy = %f' % process.accuracy(model, test_set))

            elif(task == 2):
                ts = get_input_str('enter sequence path', 'sequence.utf8')
                op = get_input_str('enter output path', 'tags.utf8')
                print('loading sequence...')
                test_set = DataSet(ts, vector_library)
                print('processing...')
                process.outputs(model, test_set, op)
            
            elif(task == 3):
                ts = get_input_str('enter training set path', 'train.utf8')
                epoch = get_input_int('enter epoch number', 1)
                training_set = DataSet(ts, vector_library)
                process.train(model, training_set, epoch, enable_gpu)
                print('saving your model...')
                torch.save(model, md)
                

            elif(task == 4):
                break

    
    print('All done! Have fun!')
