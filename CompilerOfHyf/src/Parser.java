package src;

import java.util.ArrayList;

public class Parser implements SymbolMacro {
    private ArrayList<Token> tokens;
    private int position;

    private ArrayList<Error> errors = new ArrayList<>();
    private SymbolTableStack tableStack = new SymbolTableStack();
    private Symbol curSymbol = new Symbol();
    private ArrayList<Symbol> funcFParaSymbols = new ArrayList<>();

    private boolean isInFuncDef = false;
    private int returnLines = -1;
    private int rBRaceLines = -1;
    private int inForDepth = 0;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.position = 0;
    }

    private Token getCurToken() {
        return position < tokens.size() ? tokens.get(position) : null;
    }

    //CompUnit → {Decl} {FuncDef} MainFuncDef
    public Factor parse() {
        tableStack.push(new SymbolTable());
        Factor compUnit = new Factor("CompUnit");
        while (!isMainDecl()) {
            if (!isFuncDef()) {
                compUnit.addChild(parseDecl());
            } else {
                compUnit.addChild(parseFuncDef());
            }
        }
        compUnit.addChild(parseMainFuncDef());
        tableStack.pop();
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
        } else {
            errors.add(new Error(constDecl.getEndLine(), 'i'));
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
        curSymbol = new Symbol();
        int lines = 0;
        if (getCurToken().getType().equals("IDENFR")) {
            constDef.addChild(new Factor(getCurToken()));
            lines = getCurToken().getLines();
            curSymbol.setName(getCurToken().getValue());
            curSymbol.setType(INT);
            curSymbol.setKind(CON);
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            constDef.addChild(new Factor(getCurToken()));
            position++;
            constDef.addChild(parseConstExp());
            curSymbol.addDimension();
            if (getCurToken().getValue().equals("]")) {
                constDef.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(constDef.getEndLine(), 'k'));
            }
        }
        if (getCurToken().getValue().equals("=")) {
            constDef.addChild(new Factor(getCurToken()));
            position++;
            constDef.addChild(parseConstInitVal());
        }
        if (tableStack.isReDefined(curSymbol.getName())) {
            errors.add(new Error(lines, 'b'));
        } else {
            tableStack.peek().addSymbol(curSymbol);
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
        } else {
            errors.add(new Error(varDecl.getEndLine(), 'i'));
        }
        return varDecl;
    }

    //VarDef → Ident { '[' ConstExp ']' }
    //       | Ident { '[' ConstExp ']' } '=' InitVal
    private Factor parseVarDef() {
        curSymbol = new Symbol();
        Factor varDef = new Factor("VarDef");
        int lines = 0;
        if (getCurToken().getType().equals("IDENFR")) {
            varDef.addChild(new Factor(getCurToken()));
            lines = getCurToken().getLines();
            curSymbol.setName(getCurToken().getValue());
            curSymbol.setType(INT);
            curSymbol.setKind(VAR);
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            varDef.addChild(new Factor(getCurToken()));
            position++;
            varDef.addChild(parseConstExp());
            curSymbol.addDimension();
            if (getCurToken().getValue().equals("]")) {
                varDef.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(varDef.getEndLine(), 'k'));
            }
        }
        if (getCurToken().getValue().equals("=")) {
            varDef.addChild(new Factor(getCurToken()));
            position++;
            varDef.addChild(parseInitVal());
        }
        if (tableStack.isReDefined(curSymbol.getName())) {
            errors.add(new Error(lines, 'b'));
        } else {
            tableStack.peek().addSymbol(curSymbol);
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
        curSymbol = new Symbol();
        curSymbol.setKind(FUNC);
        int lines = 0;
        Factor funcDef = new Factor("FuncDef");
        funcDef.addChild(parseFuncType());
        if (getCurToken().getType().equals("IDENFR")) {
            funcDef.addChild(new Factor(getCurToken()));
            lines = getCurToken().getLines();
            curSymbol.setName(getCurToken().getValue());
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
        } else {
            errors.add(new Error(funcDef.getEndLine(), 'j'));
        }
        if (tableStack.isReDefined(curSymbol.getName())) {
            errors.add(new Error(lines, 'b'));
        } else {
            tableStack.peek().addSymbol(curSymbol);
        }
        isInFuncDef = true;
        Symbol funcSymbol = curSymbol;
        funcDef.addChild(parseBlock());
        if (funcSymbol.getType() == VOID) {
            if (funcDef.getLastChildren().hasReturnSomething()) {
                errors.add(new Error(returnLines , 'f'));
            }
        } else if (funcSymbol.getType() == INT) {
            if (!funcDef.getLastChildren().hasReturn()) {
                errors.add(new Error(rBRaceLines , 'g'));
            }
        }

        funcFParaSymbols.clear();
        return funcDef;
    }

    //FuncType → 'void' | 'int'
    private Factor parseFuncType() {
        if (getCurToken().getValue().equals("void") ||
                getCurToken().getValue().equals("int")) {
            Factor funcType = new Factor("FuncType");
            funcType.addChild(new Factor(getCurToken()));
            if (getCurToken().getValue().equals("void")) {
                curSymbol.setType(VOID);
            } else {
                curSymbol.setType(INT);
            }
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
        Symbol paraSymbol = new Symbol();
        int lines = 0;
        funcFParam.addChild(parseBType());
        paraSymbol.setType(INT);
        paraSymbol.setKind(VAR);
        if (getCurToken().getType().equals("IDENFR")) {
            funcFParam.addChild(new Factor(getCurToken()));
            lines = getCurToken().getLines();
            paraSymbol.setName(getCurToken().getValue());
            position++;
        }
        if (getCurToken().getValue().equals("[")) {
            funcFParam.addChild(new Factor(getCurToken()));
            position++;
            paraSymbol.addDimension();
            if (getCurToken().getValue().equals("]")) {
                funcFParam.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(funcFParam.getEndLine(), 'k'));
            }
            while (getCurToken().getValue().equals("[")) {
                funcFParam.addChild(new Factor(getCurToken()));
                position++;
                funcFParam.addChild(parseConstExp());
                paraSymbol.addDimension();
                if (getCurToken().getValue().equals("]")) {
                    funcFParam.addChild(new Factor(getCurToken()));
                    position++;
                } else {
                    errors.add(new Error(funcFParam.getEndLine(), 'k'));
                }
            }
        }
        if (containsReDefinePara(paraSymbol)) {
            errors.add(new Error(lines, 'b'));
        } else {
            curSymbol.addPara(paraSymbol);
            funcFParaSymbols.add(paraSymbol);
        }
        return funcFParam;
    }

    private boolean containsReDefinePara(Symbol paraSymbol) {
        for (Symbol symbol : funcFParaSymbols) {
            if (symbol.getName().equals(paraSymbol.getName())) {
                return true;
            }
        }
        return false;
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
        } else {
            errors.add(new Error(mainFuncDef.getEndLine(), 'j'));
        }
        mainFuncDef.addChild(parseBlock());
        if (!mainFuncDef.getLastChildren().hasReturn()) {
            errors.add(new Error(mainFuncDef.getEndLine(), 'g'));
        }
        return mainFuncDef;
    }

    //Block → '{' { BlockItem } '}'
    private Factor parseBlock() {
        Factor block = new Factor("Block");
        if (getCurToken().getValue().equals("{")) {
            block.addChild(new Factor(getCurToken()));
            position++;
        }
        tableStack.push(new SymbolTable());

        if (isInFuncDef) {
            for (Symbol symbol : funcFParaSymbols) {
                tableStack.peek().addSymbol(symbol);
            }
            isInFuncDef = false;
        }

        while (!getCurToken().getValue().equals("}")) {
            block.addChild(parseBlockItem());
        }
        if (getCurToken().getValue().equals("}")) {
            block.addChild(new Factor(getCurToken()));
            rBRaceLines = getCurToken().getLines();
            tableStack.pop();
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
            }
            stmt.addChild(parseCond());
            if (getCurToken().getValue().equals(")")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'j'));
            }
            stmt.addChild(parseStmt());
            if (getCurToken().getValue().equals("else")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
                stmt.addChild(parseStmt());
            }
        } else if (nowValue.equals("for")) {
            stmt.addChild(new Factor(getCurToken()));
            inForDepth++;
            position++;
            if (getCurToken().getValue().equals("(")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            if (!getCurToken().getValue().equals(";")) {
                stmt.addChild(parseForStmt());
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            if (!getCurToken().getValue().equals(";")) {
                stmt.addChild(parseCond());
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            if (!getCurToken().getValue().equals(")")) {
                stmt.addChild(parseForStmt());
            }
            if (getCurToken().getValue().equals(")")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            stmt.addChild(parseStmt());
            inForDepth--;
        } else if (nowValue.equals("break")) {
            stmt.addChild(new Factor(getCurToken()));
            if (inForDepth <= 0) {
                errors.add(new Error(getCurToken().getLines(), 'm'));
            }
            position++;
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'i'));
            }
        } else if (nowValue.equals("continue")) {
            stmt.addChild(new Factor(getCurToken()));
            if (inForDepth <= 0) {
                errors.add(new Error(getCurToken().getLines(), 'm'));
            }
            position++;
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'i'));
            }
        } else if (nowValue.equals("return")) {
            stmt.addChild(new Factor(getCurToken()));
            returnLines = getCurToken().getLines();
            position++;
            if (!getCurToken().getValue().equals(";")) {
                stmt.addChild(parseExp());
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'i'));
            }
        } else if (nowValue.equals("printf")) {
            stmt.addChild(new Factor(getCurToken()));
            int lines = getCurToken().getLines();
            position++;
            if (getCurToken().getValue().equals("(")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            }
            int num = 0;
            if (getCurToken().getType().equals("STRCON")) {
                stmt.addChild(new Factor(getCurToken()));
                num = getCurToken().getNumOfFormatChars();
                position++;
            }
            int cnt = 0;
            while (getCurToken().getValue().equals(",")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
                stmt.addChild(parseExp());
                cnt++;
            }
            if (num != cnt) {
                errors.add(new Error(lines, 'l'));
            }
            if (getCurToken().getValue().equals(")")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'j'));
            }
            if (getCurToken().getValue().equals(";")) {
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(stmt.getEndLine(), 'i'));
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
                    Factor lVal = stmt.getLastChildren();
                    Symbol symbol = tableStack.searchSymbol(lVal.toTokenList().get(0).getValue());
                    if (symbol != null && symbol.getKind() == CON) {
                        errors.add(new Error(lVal.getBeginLine(), 'h'));
                    }
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
                    } else {
                        errors.add(new Error(stmt.getEndLine(), 'j'));
                    }
                    if (getCurToken().getValue().equals(";")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    } else {
                        errors.add(new Error(stmt.getEndLine(), 'i'));
                    }
                } else {
                    //the first
                    stmt.addChild(parseExp());
                    if (getCurToken().getValue().equals(";")) {
                        stmt.addChild(new Factor(getCurToken()));
                        position++;
                    } else {
                        errors.add(new Error(stmt.getEndLine(), 'i'));
                    }
                }
            } else if (getCurToken().getValue().equals(";")) {
                //the third
                stmt.addChild(factor);
                stmt.addChild(new Factor(getCurToken()));
                position++;
            } else {
                stmt.addChild(factor);
                errors.add(new Error(stmt.getEndLine(), 'i'));
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
            } else {
                errors.add(new Error(stmt.getEndLine(), 'i'));
            }
        }
        return stmt;
    }

    //ForStmt → LVal '=' Exp
    private Factor parseForStmt() {
        Factor forStmt = new Factor("ForStmt");
        forStmt.addChild(parseLVal());
        Factor lVal = forStmt.getLastChildren();
        Symbol symbol = tableStack.searchSymbol(lVal.toTokenList().get(0).getValue());
        if (getCurToken().getValue().equals("=")) {
            if (symbol.getKind() == CON) {
                errors.add(new Error(lVal.getEndLine(), 'h'));
            }
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
        int lines = 0;
        if (getCurToken().getType().equals("IDENFR")) {
            lVal.addChild(new Factor(getCurToken()));
            lines = getCurToken().getLines();
            Symbol symbol = tableStack.searchSymbol(getCurToken().getValue());
            if (symbol == null || symbol.isFunc()) {
                errors.add(new Error(lines, 'c'));
            }
            position++;
        }
        while (getCurToken().getValue().equals("[")) {
            lVal.addChild(new Factor(getCurToken()));
            position++;
            lVal.addChild(parseExp());
            if (getCurToken().getValue().equals("]")) {
                lVal.addChild(new Factor(getCurToken()));
                position++;
            } else {
                errors.add(new Error(lVal.getEndLine(), 'k'));
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
            Symbol symbol = tableStack.searchFuncSymbol(getCurToken().getValue());
            int lines = getCurToken().getLines();
            position++;
            if (getCurToken().getValue().equals("(")) {
                if (symbol == null) {
                    errors.add(new Error(lines, 'c'));
                }
                unaryExp.addChild(new Factor(getCurToken()));
                position++;
                int numOfPara = 0;
                if (!getCurToken().getValue().equals(")")) {
                    unaryExp.addChild(parseFuncRParams());
                    numOfPara = unaryExp.getLastChildren().getNumberOfUnLeafChildren();
                }
                if (symbol != null && numOfPara != symbol.getNumOfFuncFParas()) {
                    errors.add(new Error(lines, 'd'));
                }

                if (symbol != null && !unaryExp.getLastChildren().isLegalFuncRParams(symbol, tableStack)) {
                    errors.add(new Error(lines, 'e'));
                }

                if (getCurToken().getValue().equals(")")) {
                    unaryExp.addChild(new Factor(getCurToken()));
                    position++;
                } else {
                    errors.add(new Error(unaryExp.getEndLine(), 'j'));
                }
            } else {
                unaryExp.deleteChild();
                position--;
                unaryExp.addChild(parsePrimaryExp());
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

    public ArrayList<Error> getErrors() {
        //remove the same error that coursed by pre-read the exp in stmt
        ArrayList<Error> newErrors = new ArrayList<>();
        for (int i =0 ; i < errors.size(); i++) {
            Error error = errors.get(i);
            if (newErrors.contains(error)) {
                continue;
            } else {
                newErrors.add(error);
            }
        }

        return newErrors;
    }
}
