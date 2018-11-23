package crf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Sensor implements Serializable {
    private String feature;
    private Map<String, Integer> weights;

    public Sensor(String feature, String[] tags) {
        this.feature = feature;
        this.weights = new HashMap<>();
        for(String tag : tags)
            weights.put(tag, 0);
    }

    public int evaluate(String feature, String tag){
        int score = 0;
        for(Map.Entry<String, Integer> entry : weights.entrySet()){
            if(tag.equals(entry.getKey()) && feature.equals(this.feature)){
                score = score + entry.getValue();
            }
        }
        return score;
    }

    public void increase(String tag, int value){
        if(weights.containsKey(tag)){
            weights.put(tag, weights.get(tag) + value);
        }
    }
}
