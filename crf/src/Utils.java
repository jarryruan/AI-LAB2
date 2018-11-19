public class Utils {
    private Utils(){}

    public static float accuracy(CRFModel model){
        String[][] outputs = model.getOutputs();
        int size = model.getDataSet().numberOfSentences();
        int total = 0;
        int correct = 0;

        for(int i = 0; i < size; i++){
            Sentence sentence = model.getDataSet().get(i);
            int length = sentence.length();
            total += sentence.length();

            for(int j = 0; j < length; j++){
                if(outputs[i][j].equals(sentence.getTag(j)))
                    correct++;
            }
        }

        return 1.0f * correct / total;
    }
}
