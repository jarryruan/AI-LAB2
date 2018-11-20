public class Utils {
    private Utils(){}

    public static float accuracy(DataSet dataSet, String[][] outputs){
        int size = dataSet.numberOfSentences();
        int total = 0;
        int correct = 0;

        for(int i = 0; i < size; i++){
            Sentence sentence = dataSet.get(i);
            int length = sentence.length();
            total += sentence.length();

            for(int j = 0; j < length; j++){
                if(outputs[i][j].equals(sentence.getTag(j)))
                    correct++;
            }
        }

        return 1.0f * correct / total;
    }

    public static float accuracy(CRFModel model){
        DataSet dataSet = model.getDataSet();
        String[][] outputs = model.getOutputs();
        return accuracy(dataSet, outputs);
    }
}
