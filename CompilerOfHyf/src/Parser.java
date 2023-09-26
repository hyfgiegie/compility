package src;

import java.util.ArrayList;

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

    //CompUnit → {Decl} {FuncDef} MainFuncDef
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
                && tokens.get(position + 1).getValue().equals("main");
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
        Factor constDecl = new Factor("ConstDecl");
        if (getCurToken().getValue().equals("const")) {
            constDecl.addChild(new Factor(getCurToken()));
            position++;
        }
        constDecl.addChild(parseBType());
        constDecl.addChild(parseConstDef());
        while (getCurToken().getValue().equals(",")) {
            constDecl.addChild(new Factor(getCurToken()));
            position++;
            constDecl.addChild(parseConstDef());
        }
        if (getCurToken().getValue().equals(";")) {
            constDecl.addChild(new Factor(getCurToken()));
            position++;
        }
        return constDecl;
    }

    // BType → 'int'
    private Factor parseBType() {
        if (getCurToken().getValue().equals("int")) {
            Factor bType = new Factor("BType");
            bType.addChild(new Factor(getCurToken()));
            position++;
            return bType;
        }
        return null;
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private Factor parseConstDef() {
        Factor constDef = new Factor("ConstDef");
        if (getCurToken().getType().equals("IDENFR")) {
            constDef.addChild(new Factor(getCurToken()));
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            constDef.addChild(new Factor(getCurToken()));
            position++;
            constDef.addChild(parseConstExp());
            if (getCurToken().getValue().equals("]")) {
                constDef.addChild(new Factor(getCurToken()));
                position++;
            }
        }
        if (getCurToken().getValue().equals("=")) {
            constDef.addChild(new Factor(getCurToken()));
            position++;
            constDef.addChild(parseConstInitVal());
        }
        return constDef;
    }

    //ConstInitVal → ConstExp
    //             | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private Factor parseConstInitVal() {
        Factor constInitVal = new Factor("ConstInitVal");
        if (getCurToken().getValue().equals("{")) {
            constInitVal.addChild(new Factor(getCurToken()));
            position++;
            if (!getCurToken().getValue().equals("}")) {
                constInitVal.addChild(parseConstInitVal());
                while (getCurToken().getValue().equals(",")) {
                    constInitVal.addChild(new Factor(getCurToken()));
                    position++;
                    constInitVal.addChild(parseConstInitVal());
                }
                if (getCurToken().getValue().equals("}")) {
                    constInitVal.addChild(new Factor(getCurToken()));
                    position++;
                }
            } else {
                constInitVal.addChild(new Factor(getCurToken()));
                position++;
            }
        } else {
            constInitVal.addChild(parseConstExp());
        }
        return constInitVal;
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    private Factor parseVarDecl() {
        Factor varDecl = new Factor("VarDecl");
        varDecl.addChild(parseBType());
        varDecl.addChild(parseVarDef());
        while (getCurToken().getValue().equals(",")) {
            varDecl.addChild(new Factor(getCurToken()));
            position++;
            varDecl.addChild(parseVarDef());
        }
        if (getCurToken().getValue().equals(";")) {
            varDecl.addChild(new Factor(getCurToken()));
            position++;
        }
        return varDecl;
    }

    //VarDef → Ident { '[' ConstExp ']' }
    //       | Ident { '[' ConstExp ']' } '=' InitVal
    private Factor parseVarDef() {
        Factor varDef = new Factor("VarDef");
        if (getCurToken().getType().equals("IDENFR")) {
            varDef.addChild(new Factor(getCurToken()));
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            varDef.addChild(new Factor(getCurToken()));
            position++;
            varDef.addChild(parseConstExp());
            if (getCurToken().getValue().equals("]")) {
                varDef.addChild(new Factor(getCurToken()));
                position++;
            }
        }
        if (getCurToken().getValue().equals("=")) {
            varDef.addChild(new Factor(getCurToken()));
            position++;
            varDef.addChild(parseInitVal());
        }
        return varDef;
    }

    //InitVal → Exp
    //        | '{' [ InitVal { ',' InitVal } ] '}'
    private Factor parseInitVal() {
        Factor initVal = new Factor("InitVal");
        if (getCurToken().getValue().equals("{")) {
            initVal.addChild(new Factor(getCurToken()));
            position++;
            if (!getCurToken().getValue().equals("}")) {
                initVal.addChild(parseInitVal());
                while (getCurToken().getValue().equals(",")) {
                    initVal.addChild(new Factor(getCurToken()));
                    position++;
                    initVal.addChild(parseInitVal());
                }
                if (getCurToken().getValue().equals("}")) {
                    initVal.addChild(new Factor(getCurToken()));
                    position++;
                }
            } else {
                initVal.addChild(new Factor(getCurToken()));
                position++;
            }
        } else {
            initVal.addChild(parseExp());
        }
        return initVal;
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private Factor parseFuncDef() {
        Factor funcDef = new Factor("FuncDef");
        funcDef.addChild(parseFuncType());
        if (getCurToken().getType().equals("IDENFR")) {
            funcDef.addChild(new Factor(getCurToken()));
            position++;
        }
        if (getCurToken().getValue().equals("(")) {
            funcDef.addChild(new Factor(getCurToken()));
            position++;
        }
        if (!getCurToken().getValue().equals(")")) {
            funcDef.addChild(parseFuncFParams());
        }
        if (getCurToken().getValue().equals(")")) {
            funcDef.addChild(new Factor(getCurToken()));
            position++;
        }
        funcDef.addChild(parseBlock());
        return funcDef;
    }

    //FuncType → 'void' | 'int'
    private Factor parseFuncType() {
        if (getCurToken().getValue().equals("void") ||
                getCurToken().getValue().equals("int")) {
            Factor funcType = new Factor("FuncType");
            funcType.addChild(new Factor(getCurToken()));
            position++;
            return funcType;
        }
        return null;
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    private Factor parseFuncFParams() {
        Factor funcFParams = new Factor("FuncFParams");
        funcFParams.addChild(parseFuncFParam());
        while (getCurToken().getValue().equals(",")) {
            funcFParams.addChild(new Factor(getCurToken()));
            position++;
            funcFParams.addChild(parseFuncFParam());
        }
        return funcFParams;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Factor parseFuncFParam() {
        Factor funcFParam = new Factor("FuncFParam");
        funcFParam.addChild(parseBType());
        if (getCurToken().getType().equals("IDENFR")) {
            funcFParam.addChild(new Factor(getCurToken()));
            position++;
        }
        if (getCurToken().getValue().equals("[")) {
            funcFParam.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals("]")) {
                funcFParam.addChild(new Factor(getCurToken()));
                position++;
            }
            while (getCurToken().getValue().equals("[")) {
                funcFParam.addChild(new Factor(getCurToken()));
                position++;
                funcFParam.addChild(parseConstExp());
                if (getCurToken().getValue().equals("]")) {
                    funcFParam.addChild(new Factor(getCurToken()));
                    position++;
                }
            }
        }
        return funcFParam;
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    private Factor parseMainFuncDef() {
        Factor mainFuncDef = new Factor("MainFuncDef");
        if (getCurToken().getValue().equals("int")) {
            mainFuncDef.addChild(new Factor(getCurToken()));
            position++;
        }
        if (getCurToken().getValue().equals("main")) {
            mainFuncDef.addChild(new Factor(getCurToken()));
            position++;
        }
        if (getCurToken().getValue().equals("(")) {
            mainFuncDef.addChild(new Factor(getCurToken()));
            position++;
        }
        if (getCurToken().getValue().equals(")")) {
            mainFuncDef.addChild(new Factor(getCurToken()));
            position++;
        }
        mainFuncDef.addChild(parseBlock());
        return mainFuncDef;
    }

    //Block → '{' { BlockItem } '}'
    private Factor parseBlock() {
        Factor block = new Factor("Block");
        if (getCurToken().getValue().equals("{")) {
            block.addChild(new Factor(getCurToken()));
            position++;
        }
        while (!getCurToken().getValue().equals("}")) {
            block.addChild(parseBlockItem());
        }
        if (getCurToken().getValue().equals("}")) {
            block.addChild(new Factor(getCurToken()));
            position++;
        }
        return block;
    }

    //BlockItem → Decl | Stmt
    private Factor parseBlockItem() {
        Factor blockItem = new Factor("BlockItem");
        if (getCurToken().getValue().equals("const")
                || getCurToken().getValue().equals("int")) {
            blockItem.addChild(parseDecl());
        } else {
//            System.out.println(tokens.get(position-1).getValue() + " " + getCurToken().getValue() + "" + tokens.get(position+1).getValue() + " " + getCurToken().getLines());
            blockItem.addChild(parseStmt());
        }
        return blockItem;
    }

    /*Stmt → LVal '=' Exp ';'
         | LVal '=' 'getint''('')'';'
         | [Exp] ';'
         | Block
         | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
         | 'for' '(' [ForStmt] ';' [Cond] ';' [forStmt] ')' Stmt
         | 'break' ';'
         | 'continue' ';'
         | 'return' [Exp] ';'
         | 'printf''('FormatString{','Exp}')'';'
     */
    private Factor parseStmt() {
        Factor stmt = new Factor("Stmt");
        String nowValue = getCurToken().getValue();
        if (nowValue.equals("{")) {
            stmt.addChild(parseBlock());
        } else if (nowValue.equals("if")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals("(")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
                stmt.addChild(parseCond());
                if (getCurToken().getValue().equals(")")) {
                    stmt.addChild(new Factor(getCurToken()));
                    position++;
                    stmt.addChild(parseStmt());
                    if (getCurToken().getValue().equals("else")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                        stmt.addChild(parseStmt());
                    }
                }
            }
        } else if (nowValue.equals("for")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals("(")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
                if (!getCurToken().getValue().equals(";")) {
                    stmt.addChild(parseForStmt());
                }
                if (getCurToken().getValue().equals(";")) {
                    stmt.addChild(new Factor(getCurToken()));
                    position++;
                    if (!getCurToken().getValue().equals(";")) {
                        stmt.addChild(parseCond());
                    }
                    if (getCurToken().getValue().equals(";")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                        if (!getCurToken().getValue().equals(")")) {
                            stmt.addChild(parseForStmt());
                        }
                        if (getCurToken().getValue().equals(")")) {
                            stmt.addChild(new Factor(getCurToken()));
                            position++;
                            stmt.addChild(parseStmt());
                        }
                    }
                }
            }
        } else if (nowValue.equals("break")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (nowValue.equals("continue")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (nowValue.equals("return")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (!getCurToken().getValue().equals(";")) {
                stmt.addChild(parseExp());
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (nowValue.equals("printf")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals("(")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            if (getCurToken().getType().equals("STRCON")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            while (getCurToken().getValue().equals(",")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
                stmt.addChild(parseExp());
            }
            if (getCurToken().getValue().equals(")")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (getCurToken().getType().equals("IDENFR")) {
            //first 3 types
            int retPosition = position;
            Factor factor = parseExp();
            if (getCurToken().getValue().equals("=")) {
                //first 2
                position = retPosition;
                stmt.addChild(parseLVal());
                if (getCurToken().getValue().equals("=")) {
                    stmt.addChild(new Factor(getCurToken()));
                    position++;
                }
                if (getCurToken().getValue().equals("getint")) {
                    // the second
                    stmt.addChild(new Factor(getCurToken()));
                    position++;
                    if (getCurToken().getValue().equals("(")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    }
                    if (getCurToken().getValue().equals(")")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    }
                    if (getCurToken().getValue().equals(";")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    }
                } else {
                    //the first
                    stmt.addChild(parseExp());
                    if (getCurToken().getValue().equals(";")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    }
                }
            } else if (getCurToken().getValue().equals(";")) {
                //the third
                stmt.addChild(factor);
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (nowValue.equals(";")) {
            stmt.addChild(new Factor(getCurToken()));
            position++;
        } else if (nowValue.equals("+")
                || nowValue.equals("-")
                || nowValue.equals("!")
                || nowValue.equals("(")
                || getCurToken().getType().equals("INTCON")) {
            stmt.addChild(parseExp());
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
        }
        return stmt;
    }

    //ForStmt → LVal '=' Exp
    private Factor parseForStmt() {
        Factor forStmt = new Factor("ForStmt");
        forStmt.addChild(parseLVal());
        if (getCurToken().getValue().equals("=")) {
            forStmt.addChild(new Factor(getCurToken()));
            position++;
        }
        forStmt.addChild(parseExp());
        return forStmt;
    }

    //Exp → AddExp
    private Factor parseExp() {
        Factor exp = new Factor("Exp");
        exp.addChild(parseAddExp());
        return exp;
    }

    //Cond → LOrExp
    private Factor parseCond() {
        Factor cond = new Factor("Cond");
        cond.addChild(parseLOrExp());
        return cond;
    }

    //LVal → Ident {'[' Exp ']'}
    private Factor parseLVal() {
        Factor lVal = new Factor("LVal");
        if (getCurToken().getType().equals("IDENFR")) {
            lVal.addChild(new Factor(getCurToken()));
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            lVal.addChild(new Factor(getCurToken()));
            position++;
            lVal.addChild(parseExp());
            if (getCurToken().getValue().equals("]")) {
                lVal.addChild(new Factor(getCurToken()));
                position++;
            }
        }
        return lVal;
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    private Factor parsePrimaryExp() {
        Factor primaryExp = new Factor("PrimaryExp");
        if (getCurToken().getValue().equals("(")) {
            primaryExp.addChild(new Factor(getCurToken()));
            position++;
            primaryExp.addChild(parseExp());
            if (getCurToken().getValue().equals(")")) {
                primaryExp.addChild(new Factor(getCurToken()));
                position++;
            }
        } else if (getCurToken().getType().equals("INTCON")) {
            primaryExp.addChild(parseNumber());
        } else {
            primaryExp.addChild(parseLVal());
        }
        return primaryExp;
    }

    //Number → IntConst
    private Factor parseNumber() {
        Factor number = new Factor("Number");
        if (getCurToken().getType().equals("INTCON")) {
            number.addChild(new Factor(getCurToken()));
            position++;
        }
        return number;
    }

    //UnaryExp → PrimaryExp
    //         | Ident '(' [FuncRParams] ')'
    //         | UnaryOp UnaryExp
    private Factor parseUnaryExp() {
        Factor unaryExp = new Factor("UnaryExp");
        if (getCurToken().getValue().equals("(")
                || getCurToken().getType().equals("INTCON")) {
            unaryExp.addChild(parsePrimaryExp());
        } else if (getCurToken().getValue().equals("+")
                || getCurToken().getValue().equals("-")
                || getCurToken().getValue().equals("!")) {
            unaryExp.addChild(parseUnaryOp());
            unaryExp.addChild(parseUnaryExp());
        } else if (getCurToken().getType().equals("IDENFR")) {
            unaryExp.addChild(new Factor(getCurToken()));
            position++;
            if (getCurToken().getValue().equals("(")) {
                unaryExp.addChild(new Factor(getCurToken()));
                position++;
                if (!getCurToken().getValue().equals(")")) {
                    unaryExp.addChild(parseFuncRParams());
                }
                if (getCurToken().getValue().equals(")")) {
                    unaryExp.addChild(new Factor(getCurToken()));
                    position++;
                }
            } else if (position < tokens.size()) {
                unaryExp.deleteChild();
                position--;
                unaryExp.addChild(parsePrimaryExp());
            } else {

            }
        } else {

        }
        return unaryExp;
    }

    //UnaryOp → '+' | '−' | '!'
    private Factor parseUnaryOp() {
        if (getCurToken().getValue().equals("+") ||
                getCurToken().getValue().equals("-") ||
                getCurToken().getValue().equals("!")) {
            Factor unaryOp = new Factor("UnaryOp");
            unaryOp.addChild(new Factor(getCurToken()));
            position++;
            return unaryOp;
        }
        return null;
    }

    //FuncRParams → Exp { ',' Exp }
    private Factor parseFuncRParams() {
        Factor funcRParams = new Factor("FuncRParams");
        funcRParams.addChild(parseExp());
        while (getCurToken().getValue().equals(",")) {
            funcRParams.addChild(new Factor(getCurToken()));
            position++;
            funcRParams.addChild(parseExp());
        }
        return funcRParams;
    }

    //MulExp → UnaryExp
    //       | MulExp ('*' | '/' | '%') UnaryExp
    private Factor parseMulExp() {
        Factor mulExp = new Factor("MulExp");
        mulExp.addChild(parseUnaryExp());
        Factor ret = mulExp;
        while (getCurToken().getValue().equals("*")
                || getCurToken().getValue().equals("/")
                || getCurToken().getValue().equals("%")) {
            ret = new Factor("MulExp");
            ret.addChild(mulExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseUnaryExp());
            mulExp = ret;
        }
        return ret;
    }

    //AddExp → MulExp
    //       | MulExp ('+' | '−') AddExp
    private Factor parseAddExp() {
        Factor addExp = new Factor("AddExp");
        addExp.addChild(parseMulExp());
        Factor ret = addExp;
        while (getCurToken().getValue().equals("+")
                || getCurToken().getValue().equals("-")) {
            ret = new Factor("AddExp");
            ret.addChild(addExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseMulExp());
            addExp = ret;
        }
        return ret;
    }

    //RelExp → AddExp
    //       | AddExp ('<' | '>' | '<=' | '>=') RelExp
    private Factor parseRelExp() {
        Factor relExp = new Factor("RelExp");
        relExp.addChild(parseAddExp());
        Factor ret = relExp;
        while (getCurToken().getValue().equals("<")
                || getCurToken().getValue().equals(">")
                || getCurToken().getValue().equals("<=")
                || getCurToken().getValue().equals(">=")) {
            ret = new Factor("RelExp");
            ret.addChild(relExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseAddExp());
            relExp = ret;
        }
        return ret;
    }

    //EqExp → RelExp
    //      | RelExp ('==' | '!=') EqExp
    private Factor parseEqExp() {
        Factor eqExp = new Factor("EqExp");
        eqExp.addChild(parseRelExp());
        Factor ret = eqExp;
        while (getCurToken().getValue().equals("==")
                || getCurToken().getValue().equals("!=")) {
            ret = new Factor("EqExp");
            ret.addChild(eqExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseRelExp());
            eqExp = ret;
        }
        return ret;
    }

    //LAndExp → EqExp
    //        | EqExp '&&' LAndExp
    private Factor parseLAndExp() {
        Factor lAndExp = new Factor("LAndExp");
        lAndExp.addChild(parseEqExp());
        Factor ret = lAndExp;
        while (getCurToken().getValue().equals("&&")) {
            ret = new Factor("LAndExp");
            ret.addChild(lAndExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseEqExp());
            lAndExp = ret;
        }
        return ret;
    }

    //LOrExp → LAndExp
    //       | LAndExp '||' LOrExp
    private Factor parseLOrExp() {
        Factor lOrExp = new Factor("LOrExp");
        lOrExp.addChild(parseLAndExp());
        Factor ret = lOrExp;
        while (getCurToken().getValue().equals("||")) {
            ret = new Factor("LOrExp");
            ret.addChild(lOrExp);
            ret.addChild(new Factor(getCurToken()));
            position++;
            ret.addChild(parseLAndExp());
            lOrExp = ret;
        }
        return ret;
    }

    //ConstExp → AddExp
    private Factor parseConstExp() {
        Factor constExp = new Factor("ConstExp");
        constExp.addChild(parseAddExp());
        return constExp;
    }
}
