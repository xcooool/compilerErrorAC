import java.util.ArrayList;

public class Tag {
    public final static ArrayList<String>
            LOrExp2 = new ArrayList<String>() {
        {
            add("OR");
        }
    },
            LAndExp2 = new ArrayList<String>() {
                {
                    add("AND");
                }
            },
            EqExq2 = new ArrayList<String>() {
                {
                    add("EQL");
                    add("NEQ");
                }
            },
            RelExp2 = new ArrayList<String>() {
                {
                    add("LSS");
                    add("LEQ");
                    add("GRE");
                    add("GEQ");
                }
            },
            ConsDecl = new ArrayList<String>() {
                {
                    add("CONSTTK");
                }
            },
            MainFuncDef = new ArrayList<String>() {
                {
                    add("INTTK");
                }
            },
            AddExp2 = new ArrayList<String>() {
                {
                    add("PLUS");
                    add("MINU");
                }
            },
            MulExp2 = new ArrayList<String>() {
                {
                    add("MULT");
                    add("DIV");
                    add("MOD");
                }
            },
            UnaryOp = new ArrayList<String>() {
                {
                    add("PLUS");
                    add("MINU");
                    add("NOT");
                }
            },
            LVal = new ArrayList<String>() {
                {
                    add("IDENFR");
                }
            },
            Number = new ArrayList<String>() {
                {
                    add("INTCON");
                }
            },
            BType = new ArrayList<String>() {
                {
                    add("INTTK");
                }
            },
            Block = new ArrayList<String>() {
                {
                    add("LBRACE");
                }
            },
            FuncType = new ArrayList<String>() {
                {
                    add("VOIDTK");
                    add("INTTK");
                }
            },
            VarDef = new ArrayList<String>() {
                {
                    add("IDENFR");
                }
            },
            ConstDef = new ArrayList<String>() {
                {
                    add("IDENFR");
                }
            },
            PrimaryExp = new ArrayList<String>() {
                {
                    add("LPARENT");
                    addAll(LVal);
                    addAll(Number);
                }
            },
            UnaryExp = new ArrayList<String>() {
                {
                    addAll(PrimaryExp);
                    addAll(UnaryOp);
                    add("IDENFR");
                }
            },
            MulExp = new ArrayList<String>() {
                {
                    addAll(UnaryExp);
                }
            },
            AddExp = new ArrayList<String>() {
                {
                    addAll(MulExp);
                }
            },
            ConstExp = new ArrayList<String>() {
                {
                    addAll(AddExp);
                }
            },
            Exp = new ArrayList<String>() {
                {
                    addAll(AddExp);
                }
            },
            FuncRParams = new ArrayList<String>() {
                {
                    addAll(Exp);
                }
            },
            FuncFParam = new ArrayList<String>() {
                {
                    addAll(BType);
                }
            },
            FuncFParams = new ArrayList<String>() {
                {
                    addAll(FuncFParam);
                }
            },


    RelExp = new ArrayList<String>() {
        {
            addAll(AddExp);
        }
    },
            EqExp = new ArrayList<String>() {
                {
                    addAll(RelExp);
                }
            },
            LAndExp = new ArrayList<String>() {
                {
                    addAll(EqExp);
                }
            },
            LOrExp = new ArrayList<String>() {
                {
                    addAll(LAndExp);
                }
            },
            Cond = new ArrayList<String>() {
                {
                    addAll(LOrExp);
                }
            },
            InitVal = new ArrayList<String>() {
                {
                    add("LBRACE");
                    addAll(Exp);
                }
            },
            VarDef2 = new ArrayList<String>() {
                {
                    add("ASSIGN");
                }
            },
            ConstInitVal = new ArrayList<String>() {
                {
                    add("LBRACE");
                    addAll(ConstExp);
                }
            },
            VarDecl = new ArrayList<String>() {
                {
                    addAll(BType);
                }
            },
            FuncDef = new ArrayList<String>() {
                {
                    addAll(FuncType);
                }
            },
            Decl = new ArrayList<String>() {
                {
                    addAll(ConsDecl);
                    addAll(VarDecl);
                }
            },
            Stmt = new ArrayList<String>() {
                {
                    add("IFTK");
                    add("WHILETK");
                    add("BREAKTK");
                    add("RETURNTK");
                    add("PRINTFTK");
                    add("SEMICN");
                    add("CONTINUETK");
                    addAll(Block);
                    addAll(LVal);
                    addAll(Exp);
                }
            },
            BlockItem = new ArrayList<String>() {
                {
                    addAll(Decl);
                    addAll(Stmt);
                }
            };
}
