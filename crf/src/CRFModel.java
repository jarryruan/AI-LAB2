import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CRFModel {
    private int batchSize;
    private int numberOfWorker;
    private Template[] templates;
    private String[] tags;
    private DataSet dataSet;
    private Map<String, Sensor> sensors;
    private String[][] outputs;

    public CRFModel(int batchSize, int numberOfWorker, Template[] templates, String[] tags, DataSet dataSet) {
        this.batchSize = batchSize;
        this.numberOfWorker = numberOfWorker;
        this.templates = templates;
        this.tags = tags;
        this.dataSet = dataSet;
        this.sensors = new HashMap<>();
        this.outputs = new String[dataSet.numberOfSentences()][];
        initSensors();
    }

    public CRFModel(Template[] templates, String[] tags, DataSet dataSet) {
        this(100, 5, templates, tags, dataSet);
    }

    public Template[] getTemplates() {
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
        for(Template template : templates){
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

    public void forward() throws InterruptedException {
        int size = dataSet.numberOfSentences();
        int numberOfBatch = ((size - 1) / batchSize) + 1;

        ExecutorService service = Executors.newFixedThreadPool(numberOfWorker);

        for(int i = 0; i < numberOfBatch ; i++)
            service.execute(new ForwardTask(i * batchSize, batchSize));
        service.shutdown();

        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

    }

    public void backward(){
        int size = dataSet.numberOfSentences();
        for(Template template : templates){
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
        private int index;
        private int size;

        public ForwardTask(int index, int size) {
            this.index = index;
            this.size = size;
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
                        for(Template template : templates){
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
                    outputs[i][j] = argmax;
                }
            }
        }
    }

}
