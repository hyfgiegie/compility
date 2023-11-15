package src.ir;

import src.frontend.Factor;
import src.ir.type.Type;
import src.ir.type.single.*;
import src.ir.value.Constant;
import src.ir.value.Value;

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;

public class Utils {
    //工具类

    public static Type getDefType(Visitor visitor, String basicType, Factor factor) {
        switch (factor.getType()) {
            case "ConstDef":
            case "VarDef":
                ArrayList<Value> constants = new ArrayList<>();
                for (Factor child : factor.getChildren()) {
                    if (child.getType().equals("ConstExp")) {
                        constants.add(visitor.visitConstExp(child));
                    }
                }

                if (constants.size() == 0) {
                    //not array
                    return getBasicType(basicType);
                }
                else {
                    //array
                    ArrayList<Integer> maxLenOfEachDimension = new ArrayList<>();
                    for (Value value : constants) {
                        maxLenOfEachDimension.add(Integer.parseInt(((Constant) value).getNumericalValue()));
                    }
                    return new ArrayType(maxLenOfEachDimension, getBasicType(basicType));
                }
            case "FuncFParam":
                //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
                basicType = factor.getFirstTokenValue();
                if (factor.getChildren().size() == 2) {
                    //not array
                    return getBasicType(basicType);
                }
                else {
                    //array
                    ArrayList<Constant> consts = new ArrayList<>();
                    for (Factor child : factor.getChildren()) {
                        if (child.getType().equals("ConstExp")) {
                            consts.add((Constant) visitor.visitConstExp(child));
                        }
                    }
                    SingleType pointTo;
                    if (consts.size() == 0) {
                        pointTo = getBasicType(basicType);
                    }
                    else {
                        ArrayList<Integer> maxLen = new ArrayList<>();
                        for (Constant constant : consts) {
                            maxLen.add(Integer.parseInt(constant.getNumericalValue()));
                        }
                        pointTo = new ArrayType(maxLen, getBasicType(basicType));
                    }
                    return new PointerType(pointTo);
                }
            default:
                return null;
        }
    }

    public static SingleType getBasicType(String basicType) {
        switch (basicType) {
            case "int" :
                return new IntType();
            case "void" :
                return new VoidType();
            default:
                return null;
        }
    }

    public static int hasForStmt1(Factor forStmt) {
        if (forStmt.getChildren().get(2).getType().equals("ForStmt")) {
            return 2;
        }
        else {
            return 0;
        }
    }

    public static int hasForStmt2(Factor forStmt) {
        int size = forStmt.getChildren().size();
        if (forStmt.getChildren().get(size - 3).getType().equals("ForStmt")) {
            return size - 3;
        }
        return 0;
    }

    public static int hasCond(Factor forStmt) {
        for (int i = 0; i< forStmt.getChildren().size(); i++) {
            Factor factor = forStmt.getWhichChild(i);
            if (factor.getType().equals("Cond")) {
                return i;
            }
        }
        return 0;
    }


    public static Constant culConstant(Value v1, Value v2, String operation) {
        Constant constant = new Constant();
        if (v1.getType().getClass().equals(v2.getType().getClass()) &&
                v1.getType() instanceof IntType) {
            int num1 = Integer.parseInt(((Constant) v1).getNumericalValue());
            int num2 = Integer.parseInt(((Constant) v2).getNumericalValue());
            int result = 0;
            switch (operation) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    result = num1 / num2;
                    break;
                case "%":
                    result = num1 % num2;
                    break;
                default:
                    break;
            }
            constant.setType(new IntType());
            constant.setNumericalValue(String.valueOf(result));
        }
        return constant;
    }

}
