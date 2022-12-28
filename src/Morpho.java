
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Morpho {
    private HashMap<Integer, String> lineRawMap; //读入的文件（key为行号，value为每一行的内容，通过readfile传入）
    private int status; //当前词法分析程序所在的状态
    private String cache; //用来暂存标识符或者数字。
    //构造一个批量同时处理三个hashmap的容器（用Lexer来管理其数量等）
    private HashMap<Integer, String> typeMap; //输出的第一列
    private HashMap<Integer, String> wordMap; //输出的第二列
    private HashMap<Integer, Integer> lineMap; //输出每一行对应的行号信息
    private Integer curLine;
    private int lexer;  //输出的总数量
    private int pointer;
    public static final int INITIAL = 1; //初始状态
    public static final int IDENT = 2; //标识符状态
    public static final int INTCONST = 3; //常量状态
    public static final int FORMATSTRING = 4; //格式字符串状态
    public static final int SINGLECHAR = 5; //单字符状态
    public static final int SINGLECOMMENTS = 7; //单行注释状态
    public static final int MULTICOMMENTS = 8; //多行注释状态
    public static final int PREDOUBLECHAR = 9;
    public static final HashMap<String, String> reservedIdent = new HashMap<String,String>() { //保留字符
        {
            put("main", "MAINTK");
            put("const", "CONSTTK");
            put("int", "INTTK");
            put("break", "BREAKTK");
            put("continue", "CONTINUETK");
            put("if", "IFTK");
            put("else", "ELSETK");
            put("while", "WHILETK");
            put("getint", "GETINTTK");
            put("printf", "PRINTFTK");
            put("return", "RETURNTK");
            put("void", "VOIDTK");
        }
    };
    public static final HashMap<String, String> DoubleCharMap = new HashMap<String, String>() {
        {
            put("||", "OR");
            put("&&", "AND");
            put(">=", "GEQ");
            put("<=", "LEQ");
            put("==", "EQL");
            put("!=", "NEQ");
        }
    };
    public static final ArrayList<String> PreDoubleCharMap = new ArrayList<String>() {
        {
            add("|");
            add("&");
        }
    };

    public static final HashMap<String, String> SingleCharMap = new HashMap<String, String>() {
        {
            put("!", "NOT");
            put("+", "PLUS");
            put("-", "MINU");
            put("*", "MULT");
            put("/", "DIV");
            put("%", "MOD");
            put("<", "LSS");
            put(">", "GRE");
            put("=", "ASSIGN");
            put(";", "SEMICN");
            put(",", "COMMA");
            put("(", "LPARENT");
            put(")", "RPARENT");
            put("[", "LBRACK");
            put("]", "RBRACK");
            put("{", "LBRACE");
            put("}", "RBRACE");
        }
    };

    public Morpho() { //构造器  文件内容初始为空  状态为INITIAL  pointer为0位置
        this.lineRawMap = new HashMap<>();
        this.status = INITIAL;
        this.cache = "";
        this.typeMap = new HashMap<>();
        this.lineMap = new HashMap<>();
        this.wordMap = new HashMap<>();
        this.lexer = 0;
        this.pointer = 0;
        this.curLine = 0;
    }
    public void readFile() { //读入文件
        ReadFile file = new ReadFile();
        this.lineRawMap = file.readFileByLines("testfile.txt");
    }
    public void work() throws IOException { //分析总程序
        readFile();
        for (Map.Entry<Integer, String> entry : lineRawMap.entrySet()) {
            this.pointer = 0;
            if (status == SINGLECOMMENTS) {
                this.status = INITIAL;
                cache = "";
            }
            if(status == MULTICOMMENTS) {
                cache = "";
            }
            if(cache.length() != 0) {
                transferStatus(entry.getKey() - 1,' ');
                cache = "";
                status = INITIAL;
                this.pointer = 0;
            }
            while (this.pointer < entry.getValue().length()) {
                transferStatus(entry.getKey(), entry.getValue().charAt(this.pointer));
                pointer++;
            }
        }
        while(status != INITIAL) {
            transferStatus(lineRawMap.size(),' ');
        }
        File f=new File("error.txt");
        f.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        PrintStream printStream = new PrintStream(fileOutputStream);
        System.setOut(printStream);
        ArrayList<Token> tokens = new ArrayList<>();
        for(int i = 0;i < typeMap.size();i ++) {
//            System.out.println(typeMap.get(i) + " " + wordMap.get(i));
            Token token = new Token(typeMap.get(i),wordMap.get(i),lineMap.get(i));
            tokens.add(token);
        }
//        System.out.println("==========================================");
        //将token序列输入流传入syntax给语法分析
        SyntaxParser syntaxParser = new SyntaxParser(tokens);
        syntaxParser.parse();
    }

    public void transferStatus(Integer line, Character ch) { //状态转移
        if (this.status == INITIAL) {
            if (ch == ' ') this.status = INITIAL;
            else if (isLetter(ch) || ch == '_') {
                this.status = IDENT;
                this.cache += ch;
            } else if (isDigit(ch)) {
                this.status = INTCONST;
                this.cache += ch;
            } else if (ch == '"') {
                this.status = FORMATSTRING;
                cache += ch;
            } else if (SingleCharMap.containsKey(ch+"")) {
                this.status = SINGLECHAR;
                cache += ch;
            } else if(PreDoubleCharMap.contains(ch+"")) {
                this.status = PREDOUBLECHAR;
                cache += ch;
            }
        } else if (this.status == IDENT) {
            if (isLetter(ch) || ch == '_' || isDigit(ch)) {
                this.status = IDENT;
                this.cache += ch;
            } else {
                if (reservedIdent.containsKey(cache)) {
                    putAll(reservedIdent.get(cache), cache, line);
                } else {
                    putAll("IDENFR", cache, line);
                }
                clear();
            }
        } else if (this.status == INTCONST) {
            if (isDigit(ch)) {
                this.status = INTCONST;
                cache += ch;
            } else {
                putAll("INTCON", cache, line);
                clear();
            }
        } else if (this.status == FORMATSTRING) {
            if (ch != '"') {
                this.status = FORMATSTRING;
                cache += ch;
            } else {
                putAll("STRCON", cache+ch, line);
                clear();
                pointer++;
            }
        } else if (this.status == SINGLECHAR) {
            if (DoubleCharMap.containsKey(cache + ch)) { //处理的是>= <= != ==
                putAll(DoubleCharMap.get(cache + ch), cache + ch, line);
                cache = "";
                this.status = INITIAL;
            } else if ((cache+ch).equals("//")) {
                this.status = SINGLECOMMENTS;
                cache = "";
            } else if ((cache + ch).equals("/*")) {
                this.status = MULTICOMMENTS;
                cache = "";
            } else {
                putAll(SingleCharMap.get(cache), cache, line);
                clear();
            }
        } else if (this.status == PREDOUBLECHAR){
            if (DoubleCharMap.containsKey(cache + ch)) { //处理的是&& ||
                putAll(DoubleCharMap.get(cache + ch), cache + ch, line);
                cache = "";
                this.status = INITIAL;
            }
        }else if (this.status == SINGLECOMMENTS) {
            cache = "";
        } else if (this.status == MULTICOMMENTS) {
            if(cache.length() >= 1 && cache.charAt(cache.length() - 1) == '*' && ch == '/') {
                cache = "";
                this.status = INITIAL;
            }else {
                this.status = MULTICOMMENTS;
                cache += ch;
            }
        } else {
            this.status = INITIAL;
        }
    }

    public boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    public boolean noneZeroDigit(char ch) {
        return ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9';
    }

    public boolean isDigit(char ch) {
        return noneZeroDigit(ch) || ch == '0';
    }

    //通过自己构造一个三个hashmap的容器来统一操作put,remove等操作
    public void putAll(String type, String word, Integer line) {
        this.typeMap.put(lexer, type);
        this.wordMap.put(lexer, word);
//        if(curLine == 0) {
//            curLine = line;
//            this.lineMap.put(lexer,curLine);
//        }else if(curLine!=0 && (line == (curLine + 1))) {
//            this.lineMap.put(lexer,curLine);
//            curLine = line;
//        } else {
//            this.lineMap.put(lexer,line);
//        }
        this.lineMap.put(lexer, line);
        lexer++;
    }

    public void clear() {
        this.cache = "";
        this.status = INITIAL;
        this.pointer--;
    }

}
