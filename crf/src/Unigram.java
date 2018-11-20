public class Unigram {
    private String prefix;
    private int[] context;

    public Unigram(String prefix, int[] context) {
        this.prefix = prefix;
        this.context = context;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int[] getContext() {
        return context;
    }

    public void setContext(int[] context) {
        this.context = context;
    }

    public String feature(Sentence sentence, int index){
        StringBuilder stringBuilder = new StringBuilder(prefix);
        stringBuilder.append(':');

        for(int i = 0;i < context.length; i++){
            int row = index + context[i];
            stringBuilder.append(sentence.getWord(row));
            if(i != context.length - 1)
                stringBuilder.append('/');
        }
        return stringBuilder.toString();
    }
}
