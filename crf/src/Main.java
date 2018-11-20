import java.io.FileNotFoundException;

public class Main {
    private final static Unigram[] templates = new Unigram[]{
            new Unigram("U00", new int[]{-2, -1}),
            new Unigram("U01", new int[]{-1, 0}),
            new Unigram("U02", new int[]{0}),
            new Unigram("U03", new int[]{-1, 1}),
            new Unigram("U04", new int[]{0, 1}),
            new Unigram("U05", new int[]{1, 2}),
    };

    private final static String[] tags = new String[]{
        "B", "E", "I", "S"
    };

    private final static int epoch = 20;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        DataSet trainingSet = new DataSet("../dataset/mytrain.utf8");
        DataSet testSet = new DataSet("../dataset/mytest.utf8");

        CRFModel model = new CRFModel(templates, tags, trainingSet);

        for(int i = 0; i < epoch; i ++){
            model.forward();
            model.backward();
            System.out.println(String.format("epoch = %d, accuracy = %f", i + 1, Utils.accuracy(model)));
        }

        System.out.println(String.format("accuracy on test set = %f", Utils.accuracy(testSet, model.apply(testSet))));

    }
}
