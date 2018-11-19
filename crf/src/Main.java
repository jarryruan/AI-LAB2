import java.io.FileNotFoundException;

public class Main {
    private final static Template[] templates = new Template[]{
            new Template("U00", new int[]{-2}),
            new Template("U01", new int[]{-1}),
            new Template("U02", new int[]{0}),
            new Template("U03", new int[]{1}),
            new Template("U04", new int[]{2}),
    };

    private final static String[] tags = new String[]{
        "B", "E", "I", "S"
    };

    private final static int epoch = 10;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        DataSet dataSet = new DataSet("../dataset/train.utf8");
        CRFModel model = new CRFModel(templates, tags, dataSet);

        for(int i = 0; i < epoch; i ++){
            model.forward();
            model.backward();
            System.out.println(String.format("epoch = %d, accuracy = %f", i + 1, Utils.accuracy(model)));
        }

    }
}
