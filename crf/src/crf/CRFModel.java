package crf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CRFModel implements Serializable {
    private int batchSize;
    private int numberOfWorker;
    private UniGram[] templates;
    private String[] tags;
    private Map<String, Sensor> sensors;
    private transient String[][] outputs;

    public CRFModel(int batchSize, int numberOfWorker, UniGram[] templates, String[] tags) {
        this.batchSize = batchSize;
        this.numberOfWorker = numberOfWorker;
        this.templates = templates;
        this.tags = tags;
        this.sensors = new HashMap<>();
    }

    public UniGram[] getTemplates() {
        return templates;
    }

    public String[] getTags() {
        return tags;
    }

    public String[][] getOutputs() {
        return outputs;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public void init(DataSet dataSet){
        int size = dataSet.numberOfSentences();

        for(UniGram template : templates){
            for(int i = 0; i < size; i++){
                Sentence sentence = dataSet.get(i);
                int length = sentence.length();
                for(int j = 0; j < length; j++){
                    String feature = template.feature(sentence, j);
                    if(!sensors.containsKey(feature))
                        sensors.put(feature, new Sensor(feature, tags));
                }
            }
        }
    }

    public String[][] forward(DataSet testSet) throws InterruptedException {
        int size = testSet.numberOfSentences();
        String[][] export = new String[size][];
        calc(testSet, export);
        return export;
    }

    private void calc(DataSet dataSet, String[][] target) throws InterruptedException {
        int size = dataSet.numberOfSentences();
        int numberOfBatch = ((size - 1) / batchSize) + 1;

        ExecutorService service = Executors.newFixedThreadPool(numberOfWorker);

        for(int i = 0; i < numberOfBatch ; i++)
            service.execute(new ForwardTask(dataSet, i * batchSize, batchSize, target));
        service.shutdown();

        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }


    public void optimize(DataSet dataSet) throws InterruptedException {
        int size = dataSet.numberOfSentences();

        if(outputs == null)
            this.outputs = new String[size][];

        calc(dataSet, outputs);

        for(UniGram template : templates){
            for(int i = 0; i < size; i++){
                Sentence sentence = dataSet.get(i);
                int length = sentence.length();
                for(int j = 0; j < length; j++){
                    String feature = template.feature(sentence, j);
                    if(sensors.containsKey(feature)){
                        Sensor sensor = sensors.get(feature);
                        sensor.increase(sentence.getTag(j), 1);
                        sensor.increase(outputs[i][j], -1);
                    }
                }
            }
        }
    }

    public float accuracy(DataSet dataSet, String[][] outputs){
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


    private class ForwardTask implements Runnable{
        private DataSet dataSet;
        private int index;
        private int size;
        private String[][] target;

        public ForwardTask(DataSet dataSet, int index, int size, String[][] target) {
            this.dataSet = dataSet;
            this.index = index;
            this.size = size;
            this.target = target;
        }

        @Override
        public void run() {
            int start = index;
            int end = Math.min(index + size - 1, dataSet.numberOfSentences() - 1);

            for(int i = start; i <= end; i++){
                Sentence sentence = dataSet.get(i);
                if(target[i] == null || target[i].length != sentence.length())
                    target[i] = new String[sentence.length()];

                int length = sentence.length();

                for(int j = 0; j < length; j++){
                    int maximum = Integer.MIN_VALUE;
                    String argmax = tags[0];

                    for(String tag : tags){
                        int score = 0;
                        for(UniGram template : templates){
                            String feature = template.feature(sentence, j);
                            if(sensors.containsKey(feature)){
                                score += sensors.get(feature).evaluate(feature, tag);
                            }
                        }
                        if(score > maximum){
                            maximum = score;
                            argmax = tag;
                        }
                    }
                    target[i][j] = argmax;
                }
            }
        }
    }

}
