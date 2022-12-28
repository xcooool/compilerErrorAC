import java.lang.reflect.Array;
import java.util.ArrayList;

public class Symbol {
    private String name;
    private String objType; //CONST VAR FUNCVOID FUNCINT FUNCARAM
    private int width; //维度
    private int address; //地址
    private int decLine;//声明行号
    private ArrayList<Integer> useLineList; //使用行号
    private int paramIndex;

    private boolean isFunction;//判断是不是函数
//    private SymbolTable funcTable;//如果是函数，则这个包括int/void f()的symbol指向其对应的符号表
    private ArrayList<Symbol> paramList;
    public Symbol(String symName,String objType, int width,int address,int decLine,boolean isFunction) {
        this.name = symName;
        this.objType = objType;
        this.width = isFunction ? objType.equals("FUNCVOID") ? -1 : 0 : width;
        this.address = address;
        this.decLine = decLine;
        this.useLineList = new ArrayList<>();
        this.isFunction = isFunction;
        this.paramList = new ArrayList<>();
        this.paramIndex = 0;
    }

    public String getName() {
        return this.name;
    }
//    public void linkFuncTable(SymbolTable symbolTable) { //如果是一个函数的话，则将其指向对应的symbolTable
//        if(isFunction) {
//            this.funcTable = symbolTable;
//        }
//    }
    public void inputUse(int line) { //加入使用行号
        this.useLineList.add(line);
    }

    public void addParamList(Symbol symbol) {
        this.paramList.add(symbol);
    }
    public ArrayList<String> getParamNameList() {
        ArrayList<String> nameList = new ArrayList<>();
        for(int i = 0;i < paramList.size();i++) {
            nameList.add(paramList.get(i).getName());
        }
        return nameList;
    }
    public ArrayList<Symbol> getParamList() {
        return this.paramList;
    }
    public String getObjType() {
        return this.objType;
    }
    public int getWidth() {
        return this.width;
    }
    public boolean getIsFunction() {
        return this.isFunction;
    }
    public int getParamIndex() {
        return this.paramIndex;
    }
    public void setParamIndex(int index) {
        this.paramIndex = index;
    }
}
