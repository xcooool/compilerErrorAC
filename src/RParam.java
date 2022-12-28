import java.util.HashMap;

public class RParam {
    private String name;
    private RParam prev;
    private int paramIndex;
    private int line;
    private int inArray;
    private HashMap<Integer,Integer> paramMap;//key为顺序，value为该实参的维度

    public RParam(String name,RParam prev,int line) {
        this.name = name;
        this.prev = prev;
        this.paramIndex = 0;
        this.line = line;
        this.paramMap = new HashMap<>();
        this.inArray = 0;
    }
    public void addParamIndex() {
        this.paramIndex++;
    }
    public void putWidth(int paramIndex,int width) {
        this.paramMap.put(paramIndex,width);
    }

    public HashMap<Integer, Integer> getParamMap() {
        return paramMap;
    }
    public int getParamIndex() {
        return this.paramIndex;
    }
    public String getName() {
        return this.name;
    }
    public RParam getPrev() {
        return this.prev;
    }
    public int getLine() {
        return this.line;
    }
    public void addInArray() {
        this.inArray++;
    }
    public void subInArray() {
        this.inArray--;
    }
    public int getInArray() {
        return this.inArray;
    }
}
