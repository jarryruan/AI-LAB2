public class Utils {
    private Utils(){}

    public static float accuracy(CRFModel model){
        String[] outputs = model.getOutputs();
        int len = outputs.length;
        int correct = 0;

        for(int i = 0; i < len; i++){
            if(outputs[i].equals(model.getDataSet().getTag(i)))
                correct++;
        }

        return 1.0f * correct / len;
    }
}
