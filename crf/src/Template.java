public class Template {
    private String prefix;
    private int[][] context;

    public Template(String prefix, int[][] context) {
        this.prefix = prefix;
        this.context = context;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int[][] getContext() {
        return context;
    }

    public void setContext(int[][] context) {
        this.context = context;
    }

    public String feature(DataSet dataSet, int index){
        StringBuilder stringBuilder = new StringBuilder(prefix);
        stringBuilder.append(':');

        for(int i = 0;i < context.length; i++){
            int row = index + context[i][0];
            int column = context[i][1];
            stringBuilder.append(dataSet.get(row, column));
            if(i != context.length - 1)
                stringBuilder.append('/');
        }
        return stringBuilder.toString();
    }
}
