import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class SymbolTable {
    private HashMap<String, Symbol> table;
    private SymbolTable prev;//上一层的symboltable
    private int position;//所在上一层的位置 上一层符号表内symbol的个数为多少的时候born的这个符号表
    private int level;//所在层级
    private boolean isFunction;
    private String funcType;
    private boolean existReturn;
    private boolean defend;
    private String funcName;
//    private int paramNum; //记录函数形参的个数

    public SymbolTable(SymbolTable prev, int position,int level,boolean isFunction) {
        this.prev = prev;
        this.table = new HashMap<>();
        this.position = position;
        this.level = level;
        this.isFunction = isFunction;
        this.existReturn = false;
        this.funcType = "";
        this.funcName = "";
        this.defend = isFunction;
//        this.paramNum = 0;
    }

    public SymbolTable getPrev() {
        return this.prev;
    }

    public void inputSym(Symbol symbol) {
        this.table.put(symbol.getName(),symbol);
    }

    public int getTableSize() {
        return this.table.size();
    }
    public boolean getIsFunction() {
        return this.isFunction;
    }
//    public void setParamNum(int num) {
//        this.paramNum = num;
//    }
    public HashMap<String,Symbol> getTable() {
        return this.table;
    }

    public int getPosition() {
        return this.position;
    }
    public void setExistReturn() {
        this.existReturn = true;
    }
    public void setFuncType(String type) {
        this.funcType=  type;
    }
    public void setFuncName(String name) {
        this.funcName = name;
    }
    public void setDefend(boolean defend) {
        this.defend = defend;
    }
    public boolean getDefend() {
        return this.defend;
    }
    public String getFuncType() {
        return this.funcType;
    }
    public boolean getExistReturn() {
        return this.existReturn;
    }
    public void setIsFunc(boolean isFunction) {
        this.isFunction = isFunction;
    }
}
