import java.util.ArrayList;

public class Token {
    private String type;
    private String word;
    private Integer line;

    public Token(String type,String word,Integer line) {
        this.type = type;
        this.word = word;
        this.line = line;
    }
    public Token(String type) {
        this.type = type;
        this.word = "";
        this.line = 0;
    }
    public boolean match(ArrayList<String> conditionList) {
        return conditionList.contains(type);
    }
    public String getType() {
        return this.type;
    }
    public String getWord() {
        return this.word;
    }
    public int getLine() {
        return this.line;
    }

}
