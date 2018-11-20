import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CRFModel {
    private int batchSize;
    private int numberOfWorker;
    private Unigram[] templates;
    private String[] tags;
    private DataSet dataSet;
    private Map<String, Sensor> sensors;
    private String[][] outputs;
    private String[][] export;

    public CRFModel(int batchSize, int numberOfWorker, Unigram[] templates, String[] tags, DataSet dataSet) {
        this.batchSize = batchSize;
        this.numberOfWorker = numberOfWorker;
        this.templates = templates;
        this.tags = tags;
        this.dataSet = dataSet;
        this.sensors = new HashMap<>();

        int size = dataSet.numberOfSentences();
        this.outputs = new String[size][];
        for(int i = 0; i < size; i++)
            outputs[i] = new String[dataSet.get(i).length()];

        initSensors();
    }

    public CRFModel(Unigram[] templates, String[] tags, DataSet dataSet) {
        this(100, 5, templates, tags, dataSet);
    }

    public Unigram[] getTemplates() {
        return templates;
    }

    public String[] getTags() {
        return tags;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public String[][] getOutputs() {
        return outputs;
    }

    private void initSensors(){
        int size = dataSet.numberOfSentences();
        for(Unigram template : templates){
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

    public String[][] apply(DataSet testSet) throws InterruptedException {
        int size = testSet.numberOfSentences();
        export = new String[size][];
        for(int i = 0; i < size; i++){
            int length = testSet.get(i).length();
            export[i] = new String[length];
        }
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


    public void forward() throws InterruptedException {
        calc(dataSet, outputs);
    }


    public void backward(){
        int size = dataSet.numberOfSentences();
        for(Unigram template : templates){
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
                if(outputs[i] == null)
                    outputs[i] = new String[sentence.length()];

                int length = sentence.length();

                for(int j = 0; j < length; j++){
                    int maximum = Integer.MIN_VALUE;
                    String argmax = tags[0];

                    for(String tag : tags){
                        int score = 0;
                        for(Unigram template : templates){
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
