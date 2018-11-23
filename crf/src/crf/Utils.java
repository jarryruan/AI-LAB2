package crf;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {
    private Utils(){}

    public static UniGram[] loadTemplates(String filename) throws FileNotFoundException {
        List<UniGram> list = new ArrayList<>();

        try(Scanner scanner = new Scanner(new File(filename))){
            while(scanner.hasNext()){
                String row = scanner.nextLine();
                if(!row.isEmpty()){
                    String[] seg = row.split(" ");
                    int[] params = new int[seg.length - 1];
                    for(int i = 1; i < seg.length; i++)
                        params[i - 1] = Integer.parseInt(seg[i]);
                    list.add(new UniGram(seg[0], params));
                }
            }
            return list.toArray(new UniGram[0]);
        }
    }

    public static String[] loadSequence(String filename) throws FileNotFoundException {
        List<String> tags = new ArrayList<>();

        try(Scanner scanner = new Scanner(new File(filename))){
            while(scanner.hasNext()){
                String row = scanner.nextLine();
                if(!row.isEmpty())
                    tags.add(row);
            }
            return tags.toArray(new String[0]);
        }
    }



    public static void writeObject(Object object, String filename) throws IOException {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))){
            out.writeObject(object);
        }
    }

    public static Object readObject(String filename) throws IOException, ClassNotFoundException {
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))){
            return in.readObject();
        }
    }
}
