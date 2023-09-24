package src;

import java.util.ArrayList;
import java.util.PropertyResourceBundle;

public class Parser {
    private ArrayList<Token> tokens;
    private int position;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    private Token getCurToken() {
        return position < tokens.size() ? tokens.get(position) : null;
    }

    //CompUnit → {Decl} {FuncDef} MainDecl
    public Factor parse() {
        Factor compUnit = new Factor("CompUnit");
        while (!isMainDecl()) {
            if (!isFuncDef()) {
                compUnit.addChild(parseDecl());
            } else {
                compUnit.addChild(parseFuncDef());
            }
        }
        compUnit.addChild(parseMainFuncDef());
        return compUnit;
    }

    private boolean isMainDecl() {
        return position + 1 < tokens.size()
                && tokens.get(position).getValue().equals("int")
                && tokens.get(position+1).getValue().equals("main");
    }

    private boolean isFuncDef() {
        return position + 2 < tokens.size()
                && tokens.get(position + 1).getType().equals("IDENFR")
                && tokens.get(position + 2).getValue().equals("(");
    }

    // Decl → ConstDecl | VarDecl
    private Factor parseDecl() {
        Factor decl = new Factor("Decl");
        if (getCurToken().getValue().equals("const")) {
            decl.addChild(parseConstDecl());
        } else {
            decl.addChild(parseVarDecl());
        }
        return decl;
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private Factor parseConstDecl() {
        if (getCurToken().getValue().equals("const")) {
            position++;
        }
        Factor constDecl = new Factor("ConstDecl");
        constDecl.addChild(parseBType());
        constDecl.addChild(parseConstDef());
        while (getCurToken().getValue().equals(",")) {
            position++;
            constDecl.addChild(parseConstDef());
        }
        if (getCurToken().getValue().equals(";")) {
            position++;
        }
        return constDecl;
    }

    // BType → 'int'
    private Factor parseBType() {
        if (getCurToken().getValue().equals("int")) {
            Factor bType = new Factor("BType");
            bType.setToken(getCurToken());
            position++;
            return bType;
        }
        return null;
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Factor parseConstDef() {
        Factor constDef = new Factor("ConstDef");
        if (getCurToken().getType().equals("IDENFR")) {
            //Ident 不是 Factor ? ? ?
            position++;
        }

    }

    //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private Factor parseConstInitVal() {
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    private Factor parseVarDecl() {
    }

    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Factor parseVarDef() {
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private Factor parseInitVal() {
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private Factor parseFuncDef() {
    }

    //FuncType → 'void' | 'int'
    private Factor parseFuncType() {
        if (getCurToken().getValue().equals("void") ||
                getCurToken().getValue().equals("int")) {
            Factor funcType = new Factor("FuncType");
            funcType.setToken(getCurToken());
            position++;
            return funcType;
        }
        return null;
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    private Factor parseFuncParams() {
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Factor parseFuncParam() {
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    private Factor parseMainFuncDef() {
    }

    //Block → '{' { BlockItem } '}'
    private Factor parseBlock() {
    }

    //BlockItem → Decl | Stmt
    private Factor parseBlockItem() {
    }

    /*Stmt → LVal '=' Exp ';'
         | [Exp] ';'
         | Block
         | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
         | 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
         | 'break' ';' | 'continue' ';'
         | 'return' [Exp] ';'
         | LVal '=' 'getint''('')'';'
         | 'printf''('FormatString{','Exp}')'';'
     */
    private Factor parseStmt() {
    }

    //ForStmt → LVal '=' Exp
    private Factor parseForStmt() {
    }

    //Exp → AddExp
    private Factor parseExp() {
    }

    //Cond → LOrExp
    private Factor parseCond() {
    }

    //LVal → Ident {'[' Exp ']'}
    private Factor parseLVal() {
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    private Factor parsePrimaryExp() {
    }

    //Number → IntConst
    private Factor parseNumber() {
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private Factor parseUnaryExp() {
    }

    //UnaryOp → '+' | '−' | '!'
    private Factor parseUnaryOp() {
        if (getCurToken().getValue().equals("+") ||
        getCurToken().getValue().equals("-") ||
        getCurToken().getValue().equals("!")) {
            Factor unaryOp = new Factor("UnaryOp");
            unaryOp.setToken(getCurToken());
            position++;
            return unaryOp;
        }
        return null;
    }

    //FuncRParams → Exp { ',' Exp }
    private Factor parseFuncRParams() {
    }

    //MulExp → UnaryExp | UnaryExp ('*' | '/' | '%') MulExp
    private Factor parseMulExp() {
    }

    //AddExp → MulExp | MulExp ('+' | '−') AddExp
    private Factor parseAddExp() {
    }

    //RelExp → AddExp | AddExp ('<' | '>' | '<=' | '>=') RelExp
    private Factor parseRelExp() {
    }

    //EqExp → RelExp | RelExp ('==' | '!=') EqExp
    private Factor parseEqExp() {
    }

    //LAndExp → EqExp | EqExp '&&' LAndExp
    private Factor parseLAndExp() {
    }

    //LOrExp → LAndExp | LAndExp '||' LOrExp
    private Factor parseLOrExp() {
    }

    //ConstExp → AddExp
    private Factor parseConstExp() {
    }
}
