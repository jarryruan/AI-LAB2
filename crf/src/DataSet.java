import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DataSet {
    private String[][] data;
    private int numberOfColumns;

    public DataSet(String filename) throws FileNotFoundException {
        List<String[]> list = new ArrayList<>();

        Scanner scanner = new Scanner(new File(filename));

        while(scanner.hasNext()){
            String line = scanner.nextLine();

            if(line.trim().length() > 0){
                Scanner lineScanner = new Scanner(line);

                List<String> sample = new ArrayList<>();

                while(lineScanner.hasNext())
                    sample.add(lineScanner.next());

                numberOfColumns = Math.max(numberOfColumns, sample.size());

                list.add(sample.toArray(new String[0]));

            }else {

                list.add(new String[]{"NIL", "S"});
            }
        }

        data = list.toArray(new String[list.size()][]);
    }

    public int numberOfRows(){
        return data.length;
    }

    public int numberOfColumns(){
        return numberOfColumns;
    }

    public String get(int i, int j){
        try{
            return data[i][j];
        }catch (ArrayIndexOutOfBoundsException ex){
            return "NIL";
        }
    }

    public String getTag(int index){
        return get(index, numberOfColumns - 1);
    }
}
