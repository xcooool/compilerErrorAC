import javax.print.attribute.HashAttributeSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SyntaxParser {
    private ArrayList<Token> inputStream;
    //    private ArrayList<Token> outputStream;
    private Token curToken;
    private int index;//用来标识当前读到第几个token了
    private SymbolTable curTable;
    private int level;
    private final static int INTSIZE = 4;
    private int address;
    private Symbol curSymbol;
    private HashMap<String, SymbolTable> funcTableMap;
    private Error error;
    private SymbolTable rootTable;
    private ArrayList<Integer> paramWidthList;//存储调用实参的维度
    private HashMap<String, HashMap<Integer, Integer>> FuncParamMap; //key为函数的名称  value为这个函数对应的实参及其维数
    private ArrayList<String> curFuncStack;  //在调用的时候f(g(h()))用来储存当前的function
    private Integer inWhile=0;//用于判断是否在循环块中
    private int paramIndex = 0; //用来记录实参的顺序
    private RParam curParam; //当前的函数实参

    private int inArray = 0;
    private final static ArrayList<String> stopList = new ArrayList() {
        {
            add("<Decl>");
            add("<BType>");
            add("<Eps>");
            add("<BlockItem>");
        }
    };
    public final static ArrayList<String> reverseList = new ArrayList<String>() {
        {
            add("<MulExp2>");
            add("<AddExp2>");
            add("<RelExp2>");
            add("<EqExp2>");
            add("<LAndExp2>");
            add("<LOrExp2>");
            add("<VarDef2>");
        }
    };

    public boolean existEqual() { //判断之后是否存在等号  存在问题  如果没有分号?  应该是先检查有没有分号 还是先判断LVal
        String temp = this.curToken.getWord();
        int tpIndex = index;
        while (!temp.equals(";") && inputStream.get(tpIndex) != null) {
            temp = inputStream.get(tpIndex++).getWord();
            if (temp.equals("=")) {

                return true;
            }
        }
        return false;
    }

    public SyntaxParser(ArrayList<Token> tokens) {
        this.inputStream = tokens;
        this.index = -1;
        this.curTable = null;
        this.level = 1;
        this.address = 0;
        this.curSymbol = null;
        this.funcTableMap = new HashMap<>();
        this.error = new Error();
        this.rootTable = null;
        this.paramWidthList = new ArrayList<>();
        this.FuncParamMap = new HashMap<>();
        this.curFuncStack = new ArrayList<>();
        this.inWhile = 0;
        this.curParam = null;
    }

    public void nextToken() {
        if (index == inputStream.size() - 1) {
            curToken = null;
        } else {
            index++;
            this.curToken = inputStream.get(index);
        }
    }

    private Token getNextToken(int n) {
        return inputStream.get(index + n);
    }

    public Token getLastToken(Token token) {
        for (int i = 0; i < inputStream.size(); i++) {
            if (inputStream.get(i).equals(token)) {
                return inputStream.get(i - 1);
            }
        }
        return null;
    }

    public void parse() throws IOException {
        if (inputStream.size() == 0) {
            //输出流的token序列为零
        } else {
            Node root = CompUnit();
//            printOutStream(root);
//            for (int i = 0; i < inputStream.size(); i++) {
//                System.out.println(inputStream.get(i).getLine()+" "+inputStream.get(i).getWord());
//            }
            error.printErrMap();
        }
    }

    public void printOutStream(Node root) throws IOException {
//        File f = new File("output.txt");
//        f.createNewFile();
//        FileOutputStream fileOutputStream = new FileOutputStream(f);
//        PrintStream printStream = new PrintStream(fileOutputStream);
//        System.setOut(printStream);
//        for (int i = 0; i < outputStream.size(); i++) {
//                System.out.println(outputStream.get(i).getType()+" "+outputStream.get(i).getWord());
//        }
        //如果是处理了左递归  则为：根左右
        if (reverseList.contains(root.getToken().getType())) {
            root.print();
            for (int i = 0; i < root.getChildList().size(); i++) {
                printOutStream(root.getChildList().get(i));
            }
        } else { //否则是左右根
            for (int i = 0; i < root.getChildList().size(); i++) {
                printOutStream(root.getChildList().get(i));
            }
            if (!stopList.contains(root.getToken().getType())) {
                root.print();
            }
        }


    }

    public Node CompUnit() {
        Token rootToken = new Token("<CompUnit>");
        SymbolTable CompUnit = new SymbolTable(null, 0, level++, false);
        curTable = CompUnit;
        rootTable = CompUnit;
        Node root = new Node(rootToken); //以一个空token为root建立起一整个递归树
        nextToken();
        while (curToken.match(Tag.Decl) && !getNextToken(1).getWord().equals("main") && !getNextToken(2).getWord().equals("(")) {
            root.addChild(Decl());
        }
        while (curToken.match(Tag.FuncDef) && !getNextToken(1).getWord().equals("main")) {
            root.addChild(FuncDef());
        }
        if (curToken.match(Tag.MainFuncDef)) {
            root.addChild(MainFuncDef());
        } else {
            //错误处理:没有MainFuncDef
        }
        return root;
    }

    public Node Decl() {
        Node nDecl = new Node(new Token("<Decl>"));
        if (curToken.match(Tag.ConsDecl)) {
            nDecl.addChild(ConstDecl());
        } else if (curToken.match(Tag.VarDecl)) {
            nDecl.addChild(VarDecl());
        } else {
            //错误处理:Decl
        }
        return nDecl;
    }

    public Node ConstDecl() {
        Node nConstDecl = new Node(new Token("<ConstDecl>"));
        if (curToken.getType().equals("CONSTTK")) {
            nConstDecl.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.BType)) {
                nConstDecl.addChild(BType());
                if (curToken.match(Tag.ConstDef)) {
                    nConstDecl.addChild(ConstDef());
                    while (curToken.getWord().equals(",")) {
                        nConstDecl.addChild(new Node(curToken));
                        nextToken();
                        nConstDecl.addChild(ConstDef());
                    }
                    if (curToken.getWord().equals(";")) {
                        nConstDecl.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        //错误处理:缺少分号
                        error.checkErrI(getLastToken(curToken));
                    }
                } else {
                    //缺少constdef
                }
                ;
            } else {
                //缺少btype
            }
            ;
        } else {
            //缺少const
        }
        ;
        return nConstDecl;
    }

    public Node BType() {
        Node nBType = new Node(new Token("<BType>"));
        if (curToken.getType().equals("INTTK")) {
            nBType.addChild(new Node(curToken));
            nextToken();
        } else {
            //error
        }
        return nBType;
    }

    public Node ConstDef() {
        Node nConstDef = new Node(new Token("<ConstDef>"));
        if (curToken.getType().equals("IDENFR")) {
            nConstDef.addChild(new Node(curToken));
            String name = curToken.getWord();
            int line = curToken.getLine();
            nextToken();
            int width = 0;
            while (curToken.getWord().equals("[")) {
                width++;
                nConstDef.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.ConstExp)) {
                    nConstDef.addChild(ConstExp());
                    if (curToken.getWord().equals("]")) {
                        nConstDef.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        //错误:缺少']'
                        error.checkErrK(getLastToken(curToken));
                    }
                } else {
                    //缺少ConstExp
                }
            }
            Symbol symbol = new Symbol(name, "CONST", width, address, line, false);
            if (curTable.getTable().containsKey(name)) {
                if (curTable.getTable().get(name).getWidth() == width)
                    //判断到底有没有重定义:名称是否相同,维度是否相同
                    error.addErr(line, 'b');
            }
            curTable.inputSym(symbol);
            if (curToken.getWord().equals("=")) {
                nConstDef.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.ConstInitVal)) {
                    nConstDef.addChild(ConstInitVal());
                } else {
                    //缺少initval
                }
            } else {
                //错误:缺少'='
            }
        } else {
            //缺少ident
        }
        ;


        return nConstDef;
    }

    public Node ConstInitVal() {
        Node nConstInitVal = new Node(new Token("<ConstInitVal>"));
        if (curToken.match(Tag.ConstExp)) {
            nConstInitVal.addChild(ConstExp());
        } else if (curToken.getWord().equals("{")) {
            nConstInitVal.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.ConstInitVal)) {
                nConstInitVal.addChild(ConstInitVal());
            }
            while (curToken.getWord().equals(",")) {
                nConstInitVal.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.ConstInitVal)) {
                    nConstInitVal.addChild(ConstInitVal());
                }
            }
            if (curToken.getWord().equals("}")) {
                nConstInitVal.addChild(new Node(curToken));
                nextToken();
            } else {
                //错误:缺少大括号
            }
        }

        return nConstInitVal;
    }

    public Node VarDecl() {
        Node nVarDecl = new Node(new Token("<VarDecl>"));
        if (curToken.match(Tag.BType)) {
            nVarDecl.addChild(BType());
            if (curToken.match(Tag.VarDef)) {
                nVarDecl.addChild(VarDef());
                while (curToken.getWord().equals(",")) {
                    nVarDecl.addChild(new Node(curToken));
                    nextToken();
                    if (curToken.match(Tag.VarDef)) {
                        nVarDecl.addChild(VarDef());
                    }
                }
                if (curToken.getWord().equals(";")) {
                    nVarDecl.addChild(new Node(curToken));
                    nextToken();
                } else {
                    error.checkErrI(getLastToken(curToken));
                }
            }
        }
        return nVarDecl;
    }

    public Node VarDef() {
        Node nVarDef = new Node(new Token("<VarDef>"));
        if (curToken.getType().equals("IDENFR")) {
            nVarDef.addChild(new Node(curToken));
            String name = curToken.getWord();
            int line = curToken.getLine();
            nextToken();
            int width = 0;
            while (curToken.getWord().equals("[")) {
                width++;
                nVarDef.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.ConstExp)) {
                    nVarDef.addChild(ConstExp());
                    if (curToken.getWord().equals("]")) {
                        nVarDef.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        //缺少]
                        error.checkErrK(getLastToken(curToken));
                    }
                }
            }
            Symbol symbol = new Symbol(name, "VAR", width, address, line, false);
            if (curTable.getTable().containsKey(name)) {
                error.addErr(line, 'b');
            }
            curTable.inputSym(symbol);
            if (curToken.getWord().equals("=")) {
                nVarDef.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.InitVal)) {
                    nVarDef.addChild(InitVal());
                }
            } else {
                nVarDef.addChild(new Node(new Token("<Eps>")));
            }
        }
        return nVarDef;
    }

    public Node VarDef2() {
        Node nVarDef2 = new Node(new Token("<VarDef2>"));
        if (curToken.getWord().equals("=")) {
            nVarDef2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.InitVal)) {
                nVarDef2.addChild(InitVal());
            }
        } else {
            nVarDef2.addChild(new Node(new Token("<Eps>")));
        }
        return nVarDef2;
    }

    public Node InitVal() {
        Node nInitVal = new Node(new Token("<InitVal>"));
        if (curToken.match(Tag.Exp)) {
            nInitVal.addChild(Exp());
        } else if (curToken.getWord().equals("{")) {
            nInitVal.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.InitVal)) {
                nInitVal.addChild(InitVal());
                while (curToken.getWord().equals(",")) {
                    nInitVal.addChild(new Node(curToken));
                    nextToken();
                    if (curToken.match(Tag.InitVal)) {
                        nInitVal.addChild(InitVal());
                    }
                }
            }
            if (curToken.getWord().equals("}")) {
                nInitVal.addChild(new Node(curToken));
                nextToken();
            }
        }
        return nInitVal;
    }

    public Node FuncDef() {
        Node nFuncDef = new Node(new Token("<FuncDef>"));
        if (curToken.match(Tag.FuncType)) {
            String type = curToken.getWord();
            nFuncDef.addChild(FuncType());
            if (curToken.getType().equals("IDENFR")) {
                nFuncDef.addChild(new Node(curToken));
                String name = curToken.getWord();
                int line = curToken.getLine();
                nextToken();
                Symbol symbol = new Symbol(name,
                        type.equals("void") ? "FUNCVOID" : "FUNCINT", 0, address,
                        line, true);
                curSymbol = symbol;
                SymbolTable symbolTable = new SymbolTable(curTable, curTable.getTableSize(), level++, true);
//                symbol.linkFuncTable(symbolTable);
                symbolTable.setFuncType(type.equals("void") ? "FUNCVOID" : "FUNCINT");
                funcTableMap.put(name, symbolTable);
                if (curToken.getWord().equals("(")) {
                    nFuncDef.addChild(new Node(curToken));
                    nextToken();
                    if (curToken.match(Tag.FuncFParams)) {
                        nFuncDef.addChild(FuncFParams());
                    }
                    if (curToken.getWord().equals(")")) {
                        nFuncDef.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrJ(getLastToken(curToken));
                    }
                    //等到形参全都加到symbol里之后再将这个symbol加到curTable中
                    if (curTable.getTable().containsKey(name)) {
                        error.addErr(line, 'b');
                    }
                    //把void f (paramlist) 加入到compuUnit的符号表中
                    curTable.inputSym(symbol);
                    //把形参加到这个函数的符号表中
                    for (int i = 0; i < symbol.getParamList().size(); i++) {
                        symbolTable.inputSym(symbol.getParamList().get(i));
                    }
                    curTable = symbolTable;
                    if (curToken.match(Tag.Block)) {
                        nFuncDef.addChild(Block());
                    }
                }
            }
        }
        return nFuncDef;
    }

    public Node MainFuncDef() {
        Node nMainFuncDef = new Node(new Token("<MainFuncDef>"));
        if (curToken.getWord().equals("int")) {
            nMainFuncDef.addChild(new Node(curToken));
            SymbolTable symbolTable = new SymbolTable(curTable, curTable.getTableSize(), level++, true);
//                symbol.linkFuncTable(symbolTable);
            symbolTable.setFuncType("MAINFUNC");
            curTable = symbolTable;
            nextToken();
            if (curToken.getWord().equals("main")) {
                nMainFuncDef.addChild(new Node(curToken));
                nextToken();
                if (curToken.getWord().equals("(")) {
                    nMainFuncDef.addChild(new Node(curToken));
                    nextToken();
                    if (curToken.getWord().equals(")")) {
                        nMainFuncDef.addChild(new Node(curToken));
                        nextToken();
                        if (curToken.match(Tag.Block)) {
                            nMainFuncDef.addChild(Block());
                        }
                    }
                }
            }
        }
        return nMainFuncDef;
    }

    public Node FuncType() {
        Node nFuncType = new Node(new Token("<FuncType>"));
        if (curToken.getWord().equals("void") || curToken.getWord().equals("int")) {
            nFuncType.addChild(new Node(curToken));
            nextToken();
        }
        return nFuncType;
    }

    public Node FuncFParams() {
        Node nFuncFParams = new Node(new Token("<FuncFParams>"));
        if (curToken.match(Tag.FuncFParam)) {
            nFuncFParams.addChild(FuncFParam());
            while (curToken.getWord().equals(",")) {
                nFuncFParams.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.FuncFParam)) {
                    nFuncFParams.addChild(FuncFParam());
                }
            }
        }
        return nFuncFParams;
    }

    public Node FuncFParam() {
        Node nFuncFParam = new Node(new Token("<FuncFParam>"));
        if (curToken.match(Tag.BType)) {
            nFuncFParam.addChild(BType());
            if (curToken.getType().equals("IDENFR")) {
                nFuncFParam.addChild(new Node(curToken));
                String name = curToken.getWord();
                int line = curToken.getLine();
                nextToken();
                int width = 0;
                if (curToken.getWord().equals("[")) {
                    nFuncFParam.addChild(new Node(curToken));
                    nextToken();
                    width = 1;
                    if (curToken.getWord().equals("]")) {
                        nFuncFParam.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrK(getLastToken(curToken));
                    }
                    while (curToken.getWord().equals("[")) {
                        width++;
                        nFuncFParam.addChild(new Node(curToken));
                        nextToken();
                        if (curToken.match(Tag.ConstExp)) {
                            nFuncFParam.addChild(ConstExp());
                        }
                        if (curToken.getWord().equals("]")) {
                            nFuncFParam.addChild(new Node(curToken));
                            nextToken();
                        } else {
                            error.checkErrK(getLastToken(curToken));
                        }
                    }
                }
                Symbol symbol = new Symbol(name, "FUNCARAM", width, address, line, false);
                if (curSymbol.getParamNameList().contains(name)) {
                    error.addErr(line, 'b');
                }
                curSymbol.addParamList(symbol);
            }
        }

        return nFuncFParam;
    }

    public Node Block() {
        Node nBlock = new Node(new Token("<Block>"));
        if (!curTable.getDefend()) {
            SymbolTable symbolTable = new SymbolTable(curTable, curTable.getTableSize(), level++, false);
            curTable = symbolTable;
        } else {
            curTable.setDefend(false);//这个防御盾（不用建表int f(){}）只能有效一次 但是之后又要用到table的这个属性
        }
        if (curToken.getWord().equals("{")) {
            nBlock.addChild(new Node(curToken));
            nextToken();
            while (curToken.match(Tag.BlockItem)) {
                nBlock.addChild(BlockItem());
            }
            if (curToken.getWord().equals("}")) {
                nBlock.addChild(new Node(curToken));
                if (curTable.getIsFunction()) {
                    if ((curTable.getFuncType().equals("FUNCINT") ||
                            curTable.getFuncType().equals("MAINFUNC")) &&
                            !curTable.getExistReturn()) {
                        error.checkErrG(curToken);
                    }
                }
                nextToken();
                curTable = curTable.getPrev();
                level--;
            }
        }
        return nBlock;
    }

    public Node BlockItem() {
        Node nBlockItem = new Node(new Token("<BlockItem>"));
        if (curToken.match(Tag.Decl)) {
            nBlockItem.addChild(Decl());
        } else if (curToken.match(Tag.Stmt)) {
            nBlockItem.addChild(Stmt());
        }
        return nBlockItem;
    }

    public Node Stmt() {
        Node nStmt = new Node(new Token("<Stmt>"));
        if (curToken.getWord().equals("if")) {
            nStmt.addChild(new Node(curToken));
            nextToken();
            if (curToken.getWord().equals("(")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.Cond)) {
                    nStmt.addChild(Cond());
                    if (curToken.getWord().equals(")")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrJ(getLastToken(curToken));
                    }
                    if (curToken.match(Tag.Stmt)) {
                        nStmt.addChild(Stmt());
                        if (curToken.getWord().equals("else")) {
                            nStmt.addChild(new Node(curToken));
                            nextToken();
                            if (curToken.match(Tag.Stmt)) {
                                nStmt.addChild(Stmt());
                            }
                        }
                    }
                }
            }
        } else if (curToken.getWord().equals("while")) {
            inWhile++;
            nStmt.addChild(new Node(curToken));
            nextToken();
            if (curToken.getWord().equals("(")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.Cond)) {
                    nStmt.addChild(Cond());
                    if (curToken.getWord().equals(")")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrJ(getLastToken(curToken));
                    }
                    if (curToken.match(Tag.Stmt)) {
                        nStmt.addChild(Stmt());
                        inWhile --; //解除循环
                    }
                }
            }
        } else if (curToken.getWord().equals("break") || curToken.getWord().equals("continue")) {
            nStmt.addChild(new Node(curToken));
            if (inWhile == 0)
                error.checkErrM(curToken);
            nextToken();
            if (curToken.getWord().equals(";")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
            } else {
                error.checkErrI(getLastToken(curToken));
            }
        } else if (curToken.getWord().equals("return")) {
            nStmt.addChild(new Node(curToken));
            curTable.setExistReturn();
            nextToken();
            if (curToken.match(Tag.Exp)) {
                if (curTable.getIsFunction()) {
                    if (curTable.getFuncType().equals("FUNCVOID") &&
                            curTable.getExistReturn()) {
                        error.checkErrF(curToken);
                    }
                }
                nStmt.addChild(Exp());
            }
            if (curToken.getWord().equals(";")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
            } else {
                error.checkErrI(getLastToken(curToken));
            }
        } else if (curToken.getWord().equals("printf")) {
            nStmt.addChild(new Node(curToken));
            Token token = curToken;
            nextToken();
            if (curToken.getWord().equals("(")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
                if (curToken.getType().equals("STRCON")) {
                    nStmt.addChild(new Node(curToken));
                    //curToken就是formatString
                    error.checkErr(curToken, 'a', curTable);
                    int numD = 0;//%d的个数
                    for (int i = 0; i < curToken.getWord().length(); i++) {
                        if (curToken.getWord().charAt(i) == '%' &&
                                curToken.getWord().charAt(i + 1) == 'd') {
                            numD++;
                        }
                    }
                    nextToken();
                    int num = 0;//表达式的个数
                    while (curToken.getWord().equals(",")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                        if (curToken.match(Tag.Exp)) {
                            num++;
                            nStmt.addChild(Exp());
                        }
                    }
                    //判断printf("%d_%d",1);个数是否匹配
                    if (num != numD) {
                        error.checkErrL(token);
                    }
                    if (curToken.getWord().equals(")")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrJ(getLastToken(curToken));
                    }
                    if (curToken.getWord().equals(";")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        //缺少分号
                        error.checkErrI(getLastToken(curToken));
                    }
                }
            }
        } else if (curToken.getWord().equals(";")) {
            nStmt.addChild(new Node(curToken));
            nextToken();
        } else if (curToken.match(Tag.LVal) && existEqual()) {
            //判断是否要改变常量的值
            Symbol symbol = getSymbol(curToken, curTable, curTable.getTableSize());
            if (symbol != null && symbol.getObjType().equals("CONST")) {
                error.checkErrH(curToken);
            }
            nStmt.addChild(LVal());
            if (curToken.getWord().equals("=")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
                if (curToken.getWord().equals("getint")) {
                    nStmt.addChild(new Node(curToken));
                    nextToken();
                    if (curToken.getWord().equals("(")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                        if (curToken.getWord().equals(")")) {
                            nStmt.addChild(new Node(curToken));
                            nextToken();
                        } else {
                            error.checkErrJ(getLastToken(curToken));
                        }
                        if (curToken.getWord().equals(";")) {
                            nStmt.addChild(new Node(curToken));
                            nextToken();
                        } else {
                            error.checkErrI(getLastToken(curToken));
                        }
                    }
                } else if (curToken.match(Tag.Exp)) {
                    nStmt.addChild(Exp());
                    if (curToken.getWord().equals(";")) {
                        nStmt.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrI(getLastToken(curToken));
                    }
                }
            }
        } else if (curToken.match(Tag.Exp)) {
            nStmt.addChild(Exp());
            if (curToken.getWord().equals(";")) {
                nStmt.addChild(new Node(curToken));
                nextToken();
            } else {
                error.checkErrI(getLastToken(curToken));
            }
        } else if (curToken.match(Tag.Block)) {
            nStmt.addChild(Block());
        } else {
            error.checkErrI(getLastToken(curToken));
        }
        return nStmt;
    }

    public Node Exp() {
        Node nExp = new Node(new Token("<Exp>"));
        if (curToken.match(Tag.AddExp)) {
            nExp.addChild(AddExp());
        }
        return nExp;
    }

    public Node Cond() {
        Node nCond = new Node(new Token("<Cond>"));
        if (curToken.match(Tag.LOrExp)) {
            nCond.addChild(LOrExp());
        }
        return nCond;
    }

    public Symbol getSymbol(Token token, SymbolTable curTable, int position) {
        if (curTable != null) {
            Iterator map1it = curTable.getTable().entrySet().iterator();
            int exit = 0;
            Symbol symbol = null;
            for (int i = 0; i < position; i++) {
                Map.Entry entry = (Map.Entry) map1it.next();
                if (entry.getKey().equals(token.getWord())) {
                    exit = 1;
                    symbol = (Symbol) entry.getValue(); //这里的entry.getValue的类型是?
                }
            }
            if (exit == 0) {
                return getSymbol(token, curTable.getPrev(), curTable.getPosition());
            } else return symbol;
        }
        return null;
    }

    public Node LVal() {
        Node nLVal = new Node(new Token("<LVal>"));
        if (curToken.getType().equals("IDENFR")) {
            int width = 0;
            nLVal.addChild(new Node(curToken));
            //查看是否未定义
            if (error.checkErr(curToken, 'c', curTable)) {

            } else {
                //查找其维度
                Symbol symbol = getSymbol(curToken, curTable, curTable.getTableSize());
                if (symbol != null)
                    width = getSymbol(curToken, curTable, curTable.getTableSize()).getWidth();
            }
            nextToken();
            int widthReal = 0;
//            inArray = 0; //每当到LVal都初始化为0
            while (curToken.getWord().equals("[")) {
                widthReal++;
                inArray++;
                if(curParam != null) curParam.addInArray();;
                nLVal.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.Exp)) {
                    nLVal.addChild(Exp());
                    if (curToken.getWord().equals("]")) {
                        if(curParam != null) curParam.subInArray();
                        nLVal.addChild(new Node(curToken));
                        nextToken();
                    } else {
                        error.checkErrK(getLastToken(curToken));
                    }
                }
            }
            if (curParam != null && curParam.getInArray() == 0) {
                if (curParam.getParamMap().containsKey(curParam.getParamIndex())
                        && curParam.getParamMap().get(curParam.getParamIndex()) != width - widthReal) {
                    curParam.putWidth(curParam.getParamIndex(), 10);
                } else {
                    curParam.putWidth(curParam.getParamIndex(), width - widthReal);
                }
            }
        }
        return nLVal;
    }

    public Node PrimaryExp() {
        Node nPrimaryExp = new Node(new Token("<PrimaryExp>"));
        if (curToken.getWord().equals("(")) {
            nPrimaryExp.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.Exp)) {
                nPrimaryExp.addChild(Exp());
                if (curToken.getWord().equals(")")) {
                    nPrimaryExp.addChild(new Node(curToken));
                    nextToken();
                } else {
                    error.checkErrJ(getLastToken(curToken));
                }
            }
        } else if (curToken.match(Tag.LVal)) {
            nPrimaryExp.addChild(LVal());
        } else if (curToken.match(Tag.Number)) {
            nPrimaryExp.addChild(Number());
        }
        return nPrimaryExp;
    }

    public Node Number() {
        Node nNumber = new Node(new Token("<Number>"));
        if (curToken.getType().equals("INTCON")) {
            nNumber.addChild(new Node(curToken));
//            if(curFuncStack.size() > 0)
//            FuncParamMap.get(curFuncStack.get(curFuncStack.size() - 1)).add(0);
            nextToken();
        }
        return nNumber;
    }

    private String funcUseName = "";
    private Integer tokenLine = 0;

    public Node UnaryExp() {
        Node nUnaryExp = new Node(new Token("<UnaryExp>"));
        if (curToken.getType().equals("IDENFR") && getNextToken(1).getWord().equals("(")) {
            nUnaryExp.addChild(new Node(curToken));
            error.checkErr(curToken, 'c', rootTable); //检查函数名未定义
            curSymbol = getSymbol(curToken, rootTable, rootTable.getTableSize());
            RParam rParam = new RParam(curToken.getWord(), curParam, curToken.getLine());
            funcUseName = curToken.getWord();
            tokenLine = curToken.getLine();
            //如果是void 则维度为-1  如果是int a 则为0维 一维数组就是1维
            int width = 0;
            if(curSymbol!=null)
            width = curSymbol.getWidth();
            //可能存在函数同名的调用  例如sum(sum(1))
            if (FuncParamMap.containsKey(funcUseName))
                funcUseName = funcUseName + curFuncStack.size();
            FuncParamMap.put(funcUseName, new HashMap<>());
            if (curSymbol != null && curParam != null) {
                if (curParam.getParamMap().containsKey(curParam.getParamIndex()) &&
                        curParam.getParamMap().get(curParam.getParamIndex()) != width)
                    //冲突
                    curParam.putWidth(curParam.getParamIndex(), 10);
                else curParam.putWidth(curParam.getParamIndex(), width);
            }
            curFuncStack.add(funcUseName);

            nextToken();
            curParam = rParam;
            if (curToken.getWord().equals("(")) {
                nUnaryExp.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.FuncRParams)) {
                    nUnaryExp.addChild(FuncRParams());
                } else paramWidthList = new ArrayList<>(); //zero()这种没有参数的则其param
                if (curToken.getWord().equals(")")) {
                    nUnaryExp.addChild(new Node(curToken));
                    nextToken();
                } else {
                    error.checkErrJ(getLastToken(curToken));
                }
                error.checkErrE(curParam.getName(), curParam.getLine(), rootTable, curParam.getParamMap());
                curParam = curParam.getPrev();
            }
        } else if (curToken.match(Tag.PrimaryExp)) {
            nUnaryExp.addChild(PrimaryExp());
        } else if (curToken.match(Tag.UnaryOp)) {
            nUnaryExp.addChild(UnaryOp());
            if (curToken.match(Tag.UnaryExp)) {
                nUnaryExp.addChild(UnaryExp());
            }
        }
        return nUnaryExp;
    }

    public Node UnaryOp() {
        Node nUnaryOp = new Node(new Token("<UnaryOp>"));
        if (curToken.getWord().equals("+") || curToken.getWord().equals("-") ||
                curToken.getWord().equals("!")) {
            nUnaryOp.addChild(new Node(curToken));
            nextToken();
        }
        return nUnaryOp;
    }

    private boolean existFuncOrArray() {
        Token token = curToken;
        int lex = index;
        int stackNum = 0;
        //func(1,2,g() + 3 )= 8;  缺少右括号则下述不成立 但是如果缺少)这一行就一定不会缺少;
        while (!inputStream.get(lex).getWord().equals(",") && stackNum >= 0 &&
                //事实证明加上这个;是对的...
                !inputStream.get(lex).getWord().equals(";")) {
            if (inputStream.get(lex).getType().equals("IDENFR")) { //说明这个实参中有函数或者是a[]这种数组  也有可能是常量或者是变量啊 ...这个地方UnaryExp和LVal分别有处理
                return true;
            }
            if (inputStream.get(lex).getWord().equals("(")) {
                stackNum++;
            } else if (inputStream.get(lex).getWord().equals(")")) {
                stackNum--;
            }
            lex++;
        }
        return false; //否则这个实参没有用到函数 但是还有一个问题是void f()+1这种 这种应该不会出现在样例里？
    }

    public Node FuncRParams() {
        Node nFuncRParams = new Node(new Token("<FuncRParams>"));
        paramWidthList = new ArrayList<>();
        paramIndex = 0;
        if (curToken.match(Tag.Exp)) {
            if (!existFuncOrArray()) { //如果实参没有函数或是数组  那肯定是int类型的  比如1+2--12/4的exp
                FuncParamMap.get(curFuncStack.get(curFuncStack.size() - 1)).put(paramIndex, 0);
                curParam.putWidth(paramIndex, 0);
            }
            nFuncRParams.addChild(Exp());
            while (curToken.getWord().equals(",")) {
                paramIndex++;
                curParam.addParamIndex();
                nFuncRParams.addChild(new Node(curToken));
                nextToken();
                if (curToken.match(Tag.Exp)) {
                    if (!existFuncOrArray()) { //如果实参没有函数或是数组  那肯定是int类型的  比如1+2--12/4的exp
                        FuncParamMap.get(curFuncStack.get(curFuncStack.size() - 1)).put(paramIndex, 0);
                        curParam.putWidth(paramIndex, 0);
                    }
                    nFuncRParams.addChild(Exp());
                }
            }
        }
//        error.checkErrD(funcUseName, nFuncRParams.getChildList().size(), tokenLine, rootTable);
        return nFuncRParams;
    }

    public Node MulExp() {
        Node nMulExp = new Node(new Token("<MulExp>"));
        if (curToken.match(Tag.UnaryExp)) {
            nMulExp.addChild(UnaryExp());
            if (curToken.match(Tag.MulExp2)) {
                nMulExp.addChild(MulExp2());
            }
        }
        return nMulExp;
    }

    public Node MulExp2() {
        Node nMulExp2 = new Node(new Token("<MulExp2>"));
        if (curToken.getWord().equals("*") || curToken.getWord().equals("/") ||
                curToken.getWord().equals("%")) {
            nMulExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.UnaryExp)) {
                nMulExp2.addChild(UnaryExp());
                if (curToken.match(Tag.MulExp2)) {
                    nMulExp2.addChild(MulExp2());
                }
            }
        } else {
            nMulExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nMulExp2;
    }

    public Node AddExp() {
        Node nAddExp = new Node(new Token("<AddExp>"));
        if (curToken.match(Tag.MulExp)) {
            nAddExp.addChild(MulExp());
            if (curToken.match(Tag.AddExp2)) {
                nAddExp.addChild(AddExp2());
            }
        }
        return nAddExp;
    }

    public Node AddExp2() {
        Node nAddExp2 = new Node(new Token("<AddExp2>"));
        if (curToken.getWord().equals("+") || curToken.getWord().equals("-")) {
            nAddExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.MulExp)) {
                nAddExp2.addChild(MulExp());
                if (curToken.match(Tag.AddExp2)) {
                    nAddExp2.addChild(AddExp2());
                }
            }
        } else {
            nAddExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nAddExp2;
    }

    public Node RelExp() {
        Node nRelExp = new Node(new Token("<RelExp>"));
        if (curToken.match(Tag.AddExp)) {
            nRelExp.addChild(AddExp());
            if (curToken.match(Tag.RelExp2)) {
                nRelExp.addChild(RelExp2());
            }
        }
        return nRelExp;
    }

    public Node RelExp2() {
        Node nRelExp2 = new Node(new Token("<RelExp2>"));
        if (curToken.getWord().equals("<") ||
                curToken.getWord().equals(">") ||
                curToken.getWord().equals("<=") ||
                curToken.getWord().equals(">=")) {
            nRelExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.AddExp)) {
                nRelExp2.addChild(AddExp());
                if (curToken.match(Tag.RelExp2)) {
                    nRelExp2.addChild(RelExp2());
                }
            }
        } else {
            nRelExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nRelExp2;
    }

    public Node EqExp() {
        Node nEqExp = new Node(new Token("<EqExp>"));
        if (curToken.match(Tag.RelExp)) {
            nEqExp.addChild(RelExp());
            if (curToken.match(Tag.EqExq2)) {
                nEqExp.addChild(EqExp2());
            }
        }
        return nEqExp;
    }

    public Node EqExp2() {
        Node nEqExp2 = new Node(new Token("<EqExp2>"));
        if (curToken.getWord().equals("==") ||
                curToken.getWord().equals("!=")) {
            nEqExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.RelExp)) {
                nEqExp2.addChild(RelExp());
                if (curToken.match(Tag.EqExq2)) {
                    nEqExp2.addChild(EqExp2());
                }
            }
        } else {
            nEqExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nEqExp2;
    }

    public Node LAndExp() {
        Node nLAndExp = new Node(new Token("<LAndExp>"));
        if (curToken.match(Tag.EqExp)) {
            nLAndExp.addChild(EqExp());
            if (curToken.match(Tag.LAndExp2)) {
                nLAndExp.addChild(LAndExp2());
            }
        }
        return nLAndExp;
    }

    public Node LAndExp2() {
        Node nLAndExp2 = new Node(new Token("<LAndExp2>"));
        if (curToken.getWord().equals("&&")) {
            nLAndExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.EqExp)) {
                nLAndExp2.addChild(EqExp());
                if (curToken.match(Tag.LAndExp2)) {
                    nLAndExp2.addChild(LAndExp2());
                }
            }
        } else {
            nLAndExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nLAndExp2;
    }

    public Node LOrExp() {
        Node nLOrExp = new Node(new Token("<LOrExp>"));
        if (curToken.match(Tag.LAndExp)) {
            nLOrExp.addChild(LAndExp());
            if (curToken.match(Tag.LOrExp2)) {
                nLOrExp.addChild(LOrExp2());
            }
        }
        return nLOrExp;
    }

    public Node LOrExp2() {
        Node nLOrExp2 = new Node(new Token("<LOrExp2>"));
        if (curToken.getWord().equals("||")) {
            nLOrExp2.addChild(new Node(curToken));
            nextToken();
            if (curToken.match(Tag.LAndExp)) {
                nLOrExp2.addChild(LAndExp());
                if (curToken.match(Tag.LOrExp2)) {
                    nLOrExp2.addChild(LOrExp2());
                }
            }
        } else {
            nLOrExp2.addChild(new Node(new Token("<Eps>")));
        }
        return nLOrExp2;
    }

    public Node ConstExp() {
        Node nConstExp = new Node(new Token("<ConstExp>"));
        if (curToken.match(Tag.AddExp)) {
            nConstExp.addChild(AddExp());
        }
        return nConstExp;
    }

}
