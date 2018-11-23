package crf;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataSet {
    private Sentence[] data;

    public DataSet(String filename) throws FileNotFoundException {
        List<Sentence> list = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filename));

        List<String> words = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        while(scanner.hasNext()){
            String line = scanner.nextLine();

            if(line.trim().length() > 0){
                String[] segments = line.split(" ");
                words.add(segments[0]);
                tags.add(segments[1]);

            }else {
                if(!words.isEmpty()){
                    list.add(new Sentence(words, tags));
                    words = new ArrayList<>();
                    tags = new ArrayList<>();
                }
            }
        }

        if(!words.isEmpty()){
            list.add(new Sentence(words, tags));
        }

        data = list.toArray(new Sentence[0]);
    }

    public int numberOfSentences(){
        return data.length;
    }

    public Sentence get(int index){
        return data[index];
    }
}
