package crf;

import java.util.List;

public class Sentence {
    private String[] words;
    private String[] tags;

    public Sentence(String[] words, String[] tags) {
        this.words = words;
        this.tags = tags;
    }

    public Sentence(List<String> words, List<String> tags){
        this.words = words.toArray(new String[0]);
        this.tags = tags.toArray(new String[0]);
    }

    public String getWord(int index){
        try{
            return words[index];

        }catch (ArrayIndexOutOfBoundsException ex){
            return "NIL";
        }

    }

    public String getTag(int index) {
        try{
            return tags[index];

        }catch (ArrayIndexOutOfBoundsException ex){
            return "NIL";
        }

    }

    public int length(){
        return Math.min(words.length, tags.length);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(String word : words)
            builder.append(word);

        builder.append("/");

        for(String tag : tags)
            builder.append(tag);

        return builder.toString();
    }
}
