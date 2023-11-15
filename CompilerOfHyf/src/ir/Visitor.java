package src.ir;

import src.frontend.Factor;
import src.ir.type.FunctionType;
import src.ir.type.Type;
import src.ir.type.single.*;
import src.ir.value.*;

import java.util.ArrayList;

public class Visitor {
    private Factor root;
    private IRBuildFactory f;
    private ValueTable symTbls;
    private int inForDepth = 0;
    private ArrayList<BasicBlock> continueBlocks = new ArrayList<>();
    private ArrayList<BasicBlock> breakBlocks = new ArrayList<>();

    public Visitor(Factor root) {
        this.root = root;
        this.f = new IRBuildFactory();
        this.symTbls = ValueTable.getInstance();
    }

    public void visitCompUnit() {
        symTbls.pushSymTbl();
        symTbls.pushSymbol("getint", new Function("@getint", new IntType()));
        symTbls.pushSymbol("putint", new Function("@putint", new VoidType()));
        symTbls.pushSymbol("putch", new Function("@putch", new VoidType()));
        ArrayList<Factor> children = root.getChildren();
        for (Factor child : children) {
            switch (child.getType()) {
                case "Decl":
                    visitDecl(child, true);
                    break;
                case "FuncDef":
                    visitFuncDef(child);
                    break;
                case "MainFuncDef":
                    visitMainFuncDef(child);
                    break;
            }
        }
        symTbls.popSymTbl();
    }

    private void visitDecl(Factor decl, boolean isGlobal) {
        if (decl.getChildren().get(0).getType().equals("ConstDecl")) {
            visitConstDecl(decl.getLastChildren(), isGlobal);
        }
        else {
            visitVarDecl(decl.getLastChildren(), isGlobal);
        }
    }

    private void visitConstDecl(Factor constDecl, boolean isGlobal) {
        String basicType = constDecl.getChildren().get(1).getFirstTokenValue();
        if (isGlobal) {
            for (Factor child : constDecl.getChildren()) {
                if (child.getType().equals("ConstDef")) {
                    Value value = visitGlobalDef(child, basicType, true);
                    f.buildGlobalVar((GlobalVar) value);
                }
            }
        }
        else {
            for (Factor factor : constDecl.getChildren()) {
                if (factor.getType().equals("ConstDef")) {
                    visitLocalDef(factor, basicType, true);
                }
            }
        }
    }

    private void visitVarDecl(Factor varDecl, boolean isGlobal) {
        String basicType = varDecl.getFirstTokenValue();
        if (isGlobal) {
            for (Factor child : varDecl.getChildren()) {
                if (child.getType().equals("VarDef")) {
                    Value value = visitGlobalDef(child, basicType, false);
                    f.buildGlobalVar((GlobalVar) value);
                }
            }
        }
        else {
            for (Factor factor : varDecl.getChildren()) {
                if (factor.getType().equals("VarDef")) {
                    visitLocalDef(factor, basicType, false);
                }
            }
        }
    }

    private Value visitGlobalDef(Factor factor, String basicType, boolean isConst) {
        Type type = Utils.getDefType(this, basicType, factor);
        String ident = factor.getFirstTokenValue();
        String name = "@" + ident;
        GlobalVar globalVar = new GlobalVar(name, new PointerType((SingleType) type), isConst);

        if (factor.getLastChildren().getType().equals("ConstInitVal")
                || factor.getLastChildren().getType().equals("InitVal")) {
            Constant constant = visitGlobalInitVal(factor.getLastChildren());
            globalVar.setInitValue(constant);
        }
        symTbls.pushSymbol(ident, globalVar);
        return globalVar;
    }

    private Constant visitGlobalInitVal(Factor factor) {
        if (factor.getChildren().size() == 1) {
            Value value = visitConstExp(factor.getLastChildren());
            return (Constant) value;
        }
        else {
            ArrayList<Constant> constants = new ArrayList<>();
            for (Factor child : factor.getChildren()) {
                if (!child.getType().equals("Token")) {
                    constants.add(visitGlobalInitVal(child));
                }
            }
            Constant constant = new Constant();
            constant.setConstants(constants);
            return constant;
        }
    }

    private void visitLocalDef(Factor factor, String basicType, boolean isConst) {
        // declare
        String ident = factor.getFirstTokenValue();
        Type type = Utils.getDefType(this, basicType, factor);
        Value pointer = f.buildAllocInst(type);
        symTbls.pushSymbol(ident, pointer);

        // init
        if (factor.getLastChildren().getType().equals("ConstInitVal") ||
                factor.getLastChildren().getType().equals("InitVal")) {
            visitLocalInitVal(factor.getLastChildren(), pointer, isConst);
        }
    }

    private void visitLocalInitVal(Factor factor, Value pointer, boolean isConst) {
        //本质是做赋值操作，即计算 exp，store it to pointer
        if (factor.getChildren().size() == 1) {
            Value v;
            v = visitExp(factor.getLastChildren());
            f.buildStoreInst(v, pointer);
        }
        else {
            int cnt = 0;
            //array
            for (Factor child : factor.getChildren()) {
                if (!child.getType().equals("Token")) {
                    ArrayList<Value> offsets = new ArrayList<>();
                    offsets.add(new Constant("0"));
                    offsets.add(new Constant(cnt++ + ""));
                    Value newPtr = f.buildGetPtrInst(pointer, offsets);
                    visitLocalInitVal(child, newPtr, isConst);
                }
            }
        }
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private void visitFuncDef(Factor funcDef) {
        SingleType retType = Utils.getBasicType(funcDef.getFirstTokenValue());
        String ident = funcDef.getChildren().get(1).getFirstTokenValue();
        String name = "@" + ident;
        Function function = new Function(name, retType);
        symTbls.pushSymbol(ident, function);
        f.buildFunction(function);

        symTbls.pushSymTbl();
        if (funcDef.getChildren().size() > 5) {
            visitFuncFParams(funcDef.getChildren().get(3), function);
        }
        visitFirstBlockInFunc(funcDef.getLastChildren());
        symTbls.popSymTbl();
    }

    private void visitMainFuncDef(Factor mainFuncDef) {
        Function main = new Function("@main", new IntType());
        f.buildFunction(main);
        visitBlock(mainFuncDef.getLastChildren());
    }

    private void visitFuncFParams(Factor funcFParams, Function function) {
        for (Factor factor : funcFParams.getChildren()) {
            if (factor.getType().equals("FuncFParam")) {
                Value funcFParam = visitFuncFParam(factor);
                function.addFuncFParam(funcFParam);

                String ident = factor.getChildren().get(1).getFirstTokenValue();
                Value pointer = f.buildAllocInst(funcFParam.getType());
                pointer.isFuncParam();
                f.buildStoreInst(funcFParam, pointer);
                symTbls.pushSymbol(ident, pointer);
            }
        }
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private Value visitFuncFParam(Factor funcFParam) {
        Value value = new Value();
        value.newName();
        value.setType(Utils.getDefType(this, null, funcFParam));
        return value;
    }

    private void visitBlock(Factor block) {
        symTbls.pushSymTbl();
        for (int i = 1; i < block.getChildren().size() - 1; i++) {
            visitBlockItem(block.getChildren().get(i));
        }
        symTbls.popSymTbl();
    }

    private void visitFirstBlockInFunc(Factor block) {
        for (int i = 1; i < block.getChildren().size() - 1; i++) {
            visitBlockItem(block.getChildren().get(i));
        }
    }

    private void visitBlockItem(Factor blockItem) {
        switch (blockItem.getLastChildren().getType()) {
            case "Decl":
                visitDecl(blockItem.getLastChildren(), false);
                break;
            case "Stmt":
                visitStmt(blockItem.getLastChildren());
                break;
            default:
                break;
        }
    }

    private void visitStmt(Factor stmt) {
        Factor firstFactor = stmt.getChildren().get(0);
        if (firstFactor.getType().equals("Token")) {
            String firstTokenValue = stmt.getFirstTokenValue();
            switch (firstTokenValue) {
                case "if":
                    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
                    BasicBlock trueBlock = new BasicBlock();
                    BasicBlock falseBlock = new BasicBlock();
                    visitCond(stmt.getWhichChild(2), trueBlock, falseBlock);
                    f.buildBasicBlock(trueBlock);
                    visitStmt(stmt.getWhichChild(4));
                    if (stmt.getChildren().size() == 7) {
                        BasicBlock outBlock = new BasicBlock();
                        f.buildBrInst(null, outBlock, null);
                        f.buildBasicBlock(falseBlock);
                        visitStmt(stmt.getWhichChild(6));
                        f.buildBasicBlock(outBlock);
                    }
                    else {
                        f.buildBrInst(null, falseBlock, null);
                        f.buildBasicBlock(falseBlock);
                    }
                    break;
                case "for":
                    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
                    int for1Index = Utils.hasForStmt1(stmt);
                    int for2Index = Utils.hasForStmt2(stmt);
                    int condIndex = Utils.hasCond(stmt);

                    inForDepth++;
                    if (for1Index > 0) {
                        visitForStmt(stmt.getWhichChild(for1Index));
                    }
                    BasicBlock trueBlk = new BasicBlock();
                    BasicBlock falseBlk = new BasicBlock();
                    BasicBlock backBlk = new BasicBlock();
                    BasicBlock continueBlk = new BasicBlock();
                    f.buildBrInst(null, backBlk, null);
                    f.buildBasicBlock(backBlk);
                    if (condIndex > 0) {
                        visitCond(stmt.getWhichChild(condIndex), trueBlk, falseBlk);
                    }
                    f.buildBasicBlock(trueBlk);

                    continueBlocks.add(continueBlk);
                    breakBlocks.add(falseBlk);
                    visitStmt(stmt.getLastChildren());
                    f.buildBrInst(null, continueBlk, null);
                    f.buildBasicBlock(continueBlk);
                    if (for2Index > 0) {
                        visitForStmt(stmt.getWhichChild(for2Index));
                    }
                    f.buildBrInst(null, backBlk, null);
                    f.buildBasicBlock(falseBlk);
                    inForDepth--;
                    if (inForDepth == 0) {
                        continueBlocks = new ArrayList<>();
                        breakBlocks = new ArrayList<>();
                    }
                    break;
                case "break":
                    f.buildBrInst(null, breakBlocks.get(inForDepth - 1), null);
                    break;
                case "continue":
                    f.buildBrInst(null, continueBlocks.get(inForDepth - 1), null);
                    break;
                case "return":
                    //'return' [Exp] ';'
                    if (stmt.getChildren().size() == 3) {
                        f.buildReturnInst(visitExp(stmt.getChildren().get(1)));
                    }
                    else {
                        f.buildReturnInst(null);
                    }
                    break;
                case "printf":
                    //'printf''('FormatString{','Exp}')'';'
                    ArrayList<Value> exps = new ArrayList<>();
                    for (Factor factor : stmt.getChildren()) {
                        if (factor.getType().equals("Exp")) {
                            exps.add(visitExp(factor));
                        }
                    }
                    int cnt = 0;
                    String formatString = stmt.getChildren().get(2).getFirstTokenValue();
                    char[] chars = formatString.toCharArray();
                    for (int i = 1; i < chars.length - 1; i++) {
                        ArrayList<Value> funcRParams = new ArrayList<>();
                        if (chars[i] == '%') {
                            funcRParams.add(exps.get(cnt++));
                            f.buildCallInst(symTbls.findFunc("putint"), funcRParams);
                            i++;
                        }
                        else if (chars[i] == '\\') {
                            funcRParams.add(new Constant("10"));
                            f.buildCallInst(symTbls.findFunc("putch"), funcRParams);
                            i++;
                        }
                        else {
                            funcRParams.add(new Constant(String.valueOf((int) chars[i])));
                            f.buildCallInst(symTbls.findFunc("putch"), funcRParams);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        else if (firstFactor.getType().equals("LVal")) {
            switch (stmt.getChildren().get(2).getType()) {
                case "Exp":
                    //LVal '=' Exp ';'
                    Value pointer = visitLVal(firstFactor, false);
                    Value v = visitExp(stmt.getChildren().get(2));
                    f.buildStoreInst(v, pointer);
                    break;
                case "Token":
                    //LVal '=' 'getint''('')'';'
                    Value pointer2 = visitLVal(firstFactor, false);
                    Value value = f.buildCallInst(symTbls.findFunc("getint"), null);
                    f.buildStoreInst(value, pointer2);
                    break;
                default:
                    break;
            }
        }
        else if (firstFactor.getType().equals("Exp")) {
            visitExp(firstFactor);
        }
        else if (firstFactor.getType().equals("Block")) {
            visitBlock(firstFactor);
        }
    }

    private void visitForStmt(Factor forStmt) {
        //in fact is assign stmt
        visitStmt(forStmt);
    }

    private Value visitExp(Factor exp) {
        Value v = visitAddExp(exp.getLastChildren(), false);
        return v;
    }

    private void visitCond(Factor cond, BasicBlock b1, BasicBlock b2) {
        visitLOrExp(cond.getLastChildren(), b1, b2);
    }

    private Value visitLVal(Factor lVal, boolean fetch) {
        String ident = lVal.getFirstTokenValue();
        Value pointer = symTbls.find(ident);
        if (fetch) {
//            //ident[exp]..
//            //need to load it
//            if (lVal.getChildren().size() == 1) {
//                if (pointer.getIsFuncParam()) {
//                    return f.buildLoadInst(pointer);
//                }
//                else {
//                    if (((PointerType)pointer.getType()).getPointTo() instanceof ArrayType) {
//                        ArrayList<Value> offsets = new ArrayList<>();
//                        offsets.add(new Constant("0"));
//                        offsets.add(new Constant("0"));
//                        return f.buildGetPtrInst(pointer, offsets);
//                    }
//                    else {
//                        return f.buildLoadInst(pointer);
//                    }
//                }
//            }
//            else {
//                //ident[exp]...
//                if (!pointer.getIsFuncParam()) {
//                    ArrayList<Value> offsets = new ArrayList<>();
//                    offsets.add(new Constant("0"));
//                    for (Factor factor : lVal.getChildren()) {
//                        if (factor.getType().equals("Exp")) {
//                            offsets.add(visitExp(factor));
//                        }
//                    }
//                    Value ptr = f.buildGetPtrInst(pointer, offsets);
//                    return f.buildLoadInst(ptr);
//                }
//                else {
//                    Value v = f.buildLoadInst(pointer);
//                    ArrayList<Value> offsets = new ArrayList<>();
//                    for (Factor factor : lVal.getChildren()) {
//                        if (factor.getType().equals("Exp")) {
//                            offsets.add(visitExp(factor));
//                        }
//                    }
//                    Value ptr = f.buildGetPtrInst(v, offsets);
//                    return f.buildLoadInst(ptr);
//                }
//            }
            if (pointer.getIsFuncParam()) {
                if (lVal.getChildren().size() == 1) {
                    return f.buildLoadInst(pointer);
                }
                else {
                    ArrayList<Value> offsets = new ArrayList<>();
                    for (Factor child : lVal.getChildren()) {
                        if (child.getType().equals("Exp")) {
                            offsets.add(visitExp(child));
                        }
                    }
                    pointer = f.buildLoadInst(pointer);
                    //need to load?
                    if (offsets.size() == 2) {
                        Value newPtr = f.buildGetPtrInst(pointer, offsets);
                        return f.buildLoadInst(newPtr);
                    }
                    else {
                        if (((PointerType) pointer.getType()).getPointTo() instanceof IntType) {
                            Value newPtr = f.buildGetPtrInst(pointer, offsets);
                            return f.buildLoadInst(newPtr);
                        }
                        else {
                            offsets.add(new Constant("0"));
                            Value newPtr = f.buildGetPtrInst(pointer, offsets);
                            return newPtr;
                        }
                    }
                }
            }
            else {
                ArrayList<Value> exps = new ArrayList<>();
                for (Factor factor : lVal.getChildren()) {
                    if (factor.getType().equals("Exp")) {
                        exps.add(visitExp(factor));
                    }
                }
                if (exps.size() == 0) {
                    if (((PointerType) pointer.getType()).getPointTo() instanceof IntType) {
                        return f.buildLoadInst(pointer);
                    }
                    else {
                        exps.add(new Constant("0"));
                        exps.add(new Constant("0"));
                        return f.buildGetPtrInst(pointer, exps);
                    }
                }
                else if (exps.size() == 1) {
                    exps.add(0, new Constant("0"));
                    ArrayType arrayType = (ArrayType) ((PointerType) pointer.getType()).getPointTo();
                    if (arrayType.getInnerType() instanceof IntType) {
                        Value p = f.buildGetPtrInst(pointer, exps);
                        return f.buildLoadInst(p);
                    }
                    else {
                        exps.add(new Constant("0"));
                        return f.buildGetPtrInst(pointer, exps);
                    }
                }
                else {
                    exps.add(0, new Constant("0"));
                    Value p = f.buildGetPtrInst(pointer, exps);
                    return f.buildLoadInst(p);
                }
            }
        }
        else {
            //means need to store something into it,only to return its ptr
            if (lVal.getChildren().size() == 1) {
                return pointer;
            }
            else {
                if (!pointer.getIsFuncParam()) {
                    ArrayList<Value> offsets = new ArrayList<>();
                    offsets.add(new Constant("0"));
                    for (Factor factor : lVal.getChildren()) {
                        if (factor.getType().equals("Exp")) {
                            offsets.add(visitExp(factor));
                        }
                    }
                    return f.buildGetPtrInst(pointer, offsets);
                }
                else {
                    Value v = f.buildLoadInst(pointer);
                    ArrayList<Value> offsets = new ArrayList<>();
                    for (Factor factor : lVal.getChildren()) {
                        if (factor.getType().equals("Exp")) {
                            offsets.add(visitExp(factor));
                        }
                    }
                    return f.buildGetPtrInst(v, offsets);
                }
            }
        }
    }

    private Value visitPrimaryExp(Factor primaryExp, boolean calConst) {
        if (calConst) {
            if (primaryExp.getChildren().size() == 1) {
                // Number / LVal
                Factor child = primaryExp.getLastChildren();
                if (child.getType().equals("Number")) {
                    return new Constant(child.getFirstTokenValue());
                }
                else {
                    //LVal is Ident
                    String ident = child.getFirstTokenValue();
                    Value v = symTbls.find(ident);
                    return ((GlobalVar)v).getInitValue();
                }
            }
            else {
                //in fact is ( exp ) but can treat it as ( constExp )
                return visitConstExp(primaryExp.getChildren().get(1));
            }
        }
        else {
            if (primaryExp.getChildren().size() == 1) {
                Factor child = primaryExp.getLastChildren();
                if (child.getType().equals("LVal")) {
                    //lVal
                    return visitLVal(child, true);
                }
                else {
                    //number
                    String num = child.getFirstTokenValue();
                    Constant constant = new Constant(num);
                    return constant;
                }
            }
            else {
                //( exp )
                return visitExp(primaryExp.getChildren().get(1));
            }
        }
    }

    private Value visitUnaryExp(Factor unaryExp, boolean calConst) {
        if (calConst) {
            //only need to think primaryExp / unaryOp unaryExp
            if (unaryExp.getChildren().size() == 1) {
                return visitPrimaryExp(unaryExp.getLastChildren(), true);
            }
            else {
                Constant v1 = new Constant("0");
                Value v2 = visitUnaryExp(unaryExp.getLastChildren(), true);
                String operation = unaryExp.getFirstTokenValue();
                Value v = Utils.culConstant(v1, v2, operation);
                return v;
            }
        }
        else {
            if (unaryExp.getChildren().size() == 1) {
                //primaryExp
                return visitPrimaryExp(unaryExp.getLastChildren(), false);
            }
            else if (unaryExp.getChildren().size() == 2) {
                //unaryOp unaryExp
                String operation = unaryExp.getFirstTokenValue();
                switch (operation) {
                    case "+":
                        return visitUnaryExp(unaryExp.getLastChildren(), false);
                    case "-":
                        Constant zero = new Constant("0");
                        Value value = visitUnaryExp(unaryExp.getLastChildren(), false);
                        return f.buildSubInst(zero, value);
                    case "!":
                        Value v = visitUnaryExp(unaryExp.getLastChildren(), false);
                        if (v.getType() instanceof BooleanType) {
                            v = f.buildZextInst(v, new IntType());
                        }
                        return  f.buildIcmpInst(v, new Constant("0"), "==");
                    default:
                        return null;
                }
            }
            else {
                //Ident '(' [FuncRParams] ')'
                String ident = unaryExp.getFirstTokenValue();
                Function function = symTbls.findFunc(ident);
                ArrayList<Value> funcRParams = new ArrayList<>();
                if (unaryExp.getChildren().size() == 4) {
                    funcRParams = visitFuncRParams(unaryExp.getChildren().get(2));
                }
                Value value = f.buildCallInst(function, funcRParams);
                return value;
            }
        }
    }

    // FuncRParams → Exp { ',' Exp }
    private ArrayList<Value> visitFuncRParams(Factor funcRParams) {
        ArrayList<Value> ret = new ArrayList<>();
        for (Factor factor : funcRParams.getChildren()) {
            if (factor.getType().equals("Exp")) {
                Value value = visitExp(factor);
                ret.add(value);
            }
        }
        return ret;
    }

    private Value visitMulExp(Factor mulExp, boolean calConst) {
        if (calConst) {
            if (mulExp.getChildren().size() == 1) {
                return visitUnaryExp(mulExp.getLastChildren(), true);
            }
            else {
                Value v1 = visitMulExp(mulExp.getChildren().get(0), true);
                Value v2 = visitUnaryExp(mulExp.getLastChildren(), true);
                String operation = mulExp.getChildren().get(1).getFirstTokenValue();
                Value v = Utils.culConstant(v1, v2, operation);
                return v;
            }
        }
        else {
            if (mulExp.getChildren().size() == 1) {
                return visitUnaryExp(mulExp.getLastChildren(), false);
            }
            else {
                Value v1 = visitMulExp(mulExp.getChildren().get(0), false);
                Value v2 = visitUnaryExp(mulExp.getLastChildren(), false);
                String operation = mulExp.getChildren().get(1).getFirstTokenValue();
                switch (operation) {
                    case "*":
                        return f.buildMulInst(v1, v2);
                    case "/":
                        return f.buildSdivInst(v1, v2);
                    case "%":
                        return f.buildModInst(v1, v2);
                    default:
                        return null;
                }
            }
        }
    }

    private Value visitAddExp(Factor addExp, boolean calConst) {
        if (calConst) {
            if (addExp.getChildren().size() == 1) {
                return visitMulExp(addExp.getLastChildren(), true);
            }
            else {
                Value v1 = visitAddExp(addExp.getChildren().get(0), true);
                Value v2 = visitMulExp(addExp.getLastChildren(), true);
                String operation = addExp.getChildren().get(1).getFirstTokenValue();
                Value v = Utils.culConstant(v1, v2, operation);
                return v;
            }
        }
        else {
            if (addExp.getChildren().size() == 1) {
                return visitMulExp(addExp.getLastChildren(), false);
            }
            else {
                Value v1 = visitAddExp(addExp.getChildren().get(0), false);
                Value v2 = visitMulExp(addExp.getLastChildren(), false);
                String operation = addExp.getChildren().get(1).getFirstTokenValue();
                switch (operation) {
                    case "+":
                        return f.buildAddInst(v1, v2);
                    case "-":
                        return f.buildSubInst(v1, v2);
                    default:
                        return null;
                }
            }
        }
    }

    private Value visitRelExp(Factor relExp) {
        if (relExp.getChildren().size() == 1) {
            return visitAddExp(relExp.getLastChildren(), false);
        }
        else {
            Value v1 = visitRelExp(relExp.getWhichChild(0));
            Value v2 = visitAddExp(relExp.getWhichChild(2), false);
            if (v1.getType() instanceof BooleanType) {
                v1 = f.buildZextInst(v1, new IntType());
            }
            if (v2.getType() instanceof BooleanType) {
                v2 = f.buildZextInst(v2, new IntType());
            }
            String op = relExp.getWhichChild(1).getFirstTokenValue();
            return f.buildIcmpInst(v1, v2, op);
        }
    }

    private Value visitEqExp(Factor eqExp) {
        if (eqExp.getChildren().size() == 1) {
            return visitRelExp(eqExp.getLastChildren());
        }
        else {
            Value v1 = visitEqExp(eqExp.getWhichChild(0));
            Value v2 = visitRelExp(eqExp.getWhichChild(2));
            String op = eqExp.getWhichChild(1).getFirstTokenValue();
            if (v1.getType() instanceof BooleanType) {
                v1 = f.buildZextInst(v1, new IntType());
            }
            if (v2.getType() instanceof BooleanType) {
                v2 = f.buildZextInst(v2, new IntType());
            }
            return f.buildIcmpInst(v1, v2, op);
        }
    }

    private void visitLAndExp(Factor lAndExp, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (lAndExp.getChildren().size() == 1) {
            Value v = visitEqExp(lAndExp.getLastChildren());
            if (v.getType() instanceof IntType) {
                v = f.buildIcmpInst(v, new Constant("0"), "!=");
            }
            f.buildBrInst(v, trueBlock, falseBlock);
        }
        else {
            BasicBlock trueBlock2 = new BasicBlock();
            visitLAndExp(lAndExp.getWhichChild(0), trueBlock2, falseBlock);
            f.buildBasicBlock(trueBlock2);
            Value v = visitEqExp(lAndExp.getWhichChild(2));
            if (v.getType() instanceof IntType) {
                v = f.buildIcmpInst(v, new Constant("0"), "!=");
            }
            f.buildBrInst(v, trueBlock, falseBlock);
        }
    }

    private void visitLOrExp(Factor lOrExp, BasicBlock trueBlock, BasicBlock falseBlock) {
        if (lOrExp.getChildren().size() == 1) {
            visitLAndExp(lOrExp.getLastChildren(), trueBlock, falseBlock);
        }
        else {
            BasicBlock falseBlock2 = new BasicBlock();
            visitLOrExp(lOrExp.getWhichChild(0), trueBlock, falseBlock2);
            f.buildBasicBlock(falseBlock2);
            visitLAndExp(lOrExp.getWhichChild(2), trueBlock, falseBlock);
        }
    }

    public Value visitConstExp(Factor constExp) {
        Value v = visitAddExp(constExp.getLastChildren(), true);
        return v;
    }
}
