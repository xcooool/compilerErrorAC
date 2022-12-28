import java.util.*;

public class Error {
    private HashMap<Integer,Character> errMap;
    public Error() {
        this.errMap = new HashMap<>();
    }
    public boolean checkErr(Token token,char ch,SymbolTable table) {
        if(ch == 'a') {
            String str = token.getWord();
            for (int i = 1; i < str.length()-1;i ++) {
                char c = str.charAt(i);
                if(Integer.valueOf(c) < 32 || Integer.valueOf(c) > 126 ||
                        (Integer.valueOf(c) > 33 && Integer.valueOf(c) < 40 && Integer.valueOf(c) != 37) ||
                        (c == '%' && str.charAt(i+1) != 'd') ||
                        (c == '\\' && str.charAt(i+1) != 'n') ) {
                    errMap.put(token.getLine(),'a');
                    return true;
                }
            }
        }else if(ch == 'c') {
            if(checkErrC(token,table,table.getTableSize())) {
                errMap.put(token.getLine(), 'c');
                return true;
            }

        } else return false;
        return false;
    }
    public void addErr(int line,char ch) {
        this.errMap.put(line,ch);
    }

    public boolean checkErrC(Token token,SymbolTable table,int position) {
        if(table!=null) {
            Iterator map1it=table.getTable().entrySet().iterator();
            int exit = 0;
            for (int i = 0;i < table.getTableSize();i ++) {
                Map.Entry entry=(Map.Entry) map1it.next();
                if(entry.getKey().equals(token.getWord())) {
                    exit = 1;
                }
            }
            if(exit == 0) {
                return checkErrC(token,table.getPrev(),table.getPosition());
            } else return false;//已定义
        } else return true; //到了顶部了还没找到说明未定义
    }
    public void checkErrD(String name,int num,int line,SymbolTable table) {
        HashMap<String,Symbol> symTable = table.getTable();
        if(symTable.containsKey(name)) {
            if(symTable.get(name).getParamNameList().size() != num) {
                this.errMap.put(line,'d');
            }
        } else {
            this.errMap.put(line,'c');
        }
    }
    public void checkErrE(String name,int line,SymbolTable table,HashMap<Integer,Integer> widthList) { //key是实参的顺序 value是实参的维度
        ArrayList<Integer> decWidthList = new ArrayList<>();
        if(!table.getTable().containsKey(name)) {
            errMap.put(line, 'c');
        } else {
            for(int i = 0;i < table.getTable().get(name).getParamList().size() ;i ++) {
                decWidthList.add(table.getTable().get(name).getParamList().get(i).getWidth());
            }
            if(widthList.size() == 1 && widthList.get(0) == -1 && decWidthList.size() == 0) {
                //void f();void g(); f(g());
            }else{
                if(decWidthList.size() != widthList.size()) {
                    this.errMap.put(line,'d');
                } else
                {
                    for(int i = 0;i < decWidthList.size();i ++) {
                        if(!decWidthList.get(i).equals(widthList.get(i))) {
                            this.errMap.put(line,'e');
                            break;
                        }
                    }
                }
            }
        }

    }
    public void checkErrF(Token token) {
        this.errMap.put(token.getLine(),'f');
    }
    public void checkErrG(Token token) {
        this.errMap.put(token.getLine(),'g');
    }
    public void checkErrH(Token token) {
        this.errMap.put(token.getLine(),'h');
    }
    public void checkErrK(Token token) {
        this.errMap.put(token.getLine(),'k');
    }
    public void printErrMap() {
        TreeMap<Integer,Character> sortedMap = new TreeMap<>(errMap);
//        sortedMap.putAll(errMap);
        for(Map.Entry<Integer,Character> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey() + " "+entry.getValue());
        }
    }
    public void checkErrI(Token token) {
        this.errMap.put(token.getLine(),'i');
    }

    public void checkErrJ(Token token) {
        this.errMap.put(token.getLine(),'j');
    }
    public void checkErrL(Token token) {
        this.errMap.put(token.getLine(),'l');
    }
    public void checkErrM(Token token) {
        this.errMap.put(token.getLine(),'m');
    }
    public void addErr(Token token,char c) {
        this.errMap.put(token.getLine(),c);
    }
}
