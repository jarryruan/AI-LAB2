import crf.CRFModel;
import crf.DataSet;
import crf.Utils;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    private static int getInputInteger(String info, int defaultValue){
        System.out.print(String.format("%s (default=%d):", info, defaultValue));
        String in = scanner.nextLine();
        try{
            if(in.isEmpty())
                return defaultValue;
            return Integer.parseInt(in);
        }catch (NumberFormatException ex){
            return getInputInteger(info, defaultValue);
        }
    }

    private static String getInputString(String info, String defaultValue){
        System.out.print(String.format("%s (default=\"%s\"):", info, defaultValue));
        String in = scanner.nextLine();
        if(!in.isEmpty())
            return in;
        else return defaultValue;
    }

    private static void train() throws IOException, InterruptedException {
        String trainingSetPath = getInputString("enter training set path", "train.utf8");
        String savingPath = getInputString("enter model saving location", "model.crf");
        String templatesPath = getInputString("enter template path:", "template.utf8");
        String tagsPath = getInputString("enter tag list path:", "labels.utf8");
        int batchSize = getInputInteger("enter batch size",  1000);
        int numberOfWorker = getInputInteger("enter number of workers", 4);
        int epoch = getInputInteger("enter number of iteration", 10);

        System.out.println("Loading dataset...");
        DataSet trainingSet = new DataSet(trainingSetPath);
        CRFModel model = new CRFModel(batchSize, numberOfWorker, Utils.loadTemplates(templatesPath), Utils.loadSequence(tagsPath));
        model.init(trainingSet);

        System.out.println("Start training...");
        for(int i = 0; i < epoch; i ++){
            model.optimize(trainingSet);
            System.out.println(String.format("epoch = %d, accuracy = %f", i + 1, model.accuracy(trainingSet, model.getOutputs())));
        }
        
        Utils.writeObject(model, savingPath);
    }

    private static void load() throws IOException, ClassNotFoundException, InterruptedException {
        String modelPath = getInputString("enter your model path", "model.crf");

        System.out.println("Loading model...");
        CRFModel model = (CRFModel) Utils.readObject(modelPath);

        while(true){
            System.out.println("(1) Test Test Set Accuracy");
            System.out.println("(2) Calculate Sequence Output");
            System.out.println("(3) Exit\n");

            int task = getInputInteger("enter your task:", 1);

            if(task == 1){
                String datasetPath = getInputString("enter test set path:", "test.utf8");
                DataSet testSet = new DataSet(datasetPath);
                float accuracy = model.accuracy(testSet, model.forward(testSet));
                System.out.println(String.format("accuracy = %f", accuracy));

            }else if(task == 2){
                String sequencePath = getInputString("enter sequence path:", "sequence.utf8");
                String[] sequence = Utils.loadSequence(sequencePath);


            }else if(task == 3){
                break;
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        System.out.println("(1) Train New Model");
        System.out.println("(2) Load Exist Model");
        System.out.println("(3) Exit\n");

        int task = getInputInteger("Select your task", 1);

        switch (task){
            case 1:
                train();
                break;
            case 2:
                load();
                break;
            case 3:
                scanner.close();
                System.exit(0);
        }

        System.out.println("All done! Have fun!");
        scanner.close();
    }
}
