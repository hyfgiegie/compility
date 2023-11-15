package src.ir;

import src.ir.value.BasicBlock;
import src.ir.value.Constant;
import src.ir.value.Function;
import src.ir.value.GlobalVar;

import java.util.ArrayList;

public class IRModule {
    private ArrayList<GlobalVar> globalVars = new ArrayList<>();
    private ArrayList<Function> functions = new ArrayList<>();
    private static IRModule singleTon;

    private IRModule() {
    }

    public static IRModule getInstance() {
        if (singleTon == null) {
            singleTon = new IRModule();
        }
        return singleTon;
    }

    public void addGlobalVar(GlobalVar globalVar) {
        globalVars.add(globalVar);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public Function getCurFunction() {
        if (functions != null && functions.size() > 0) {
            return functions.get(functions.size() - 1);
        }
        else {
            return null;
        }
    }

    public BasicBlock getCurBasicBlock() {
        Function function = getCurFunction();
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        return basicBlocks.get(basicBlocks.size() - 1);
    }

    @Override
    public String toString() {
        String s = "";
        s += "declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n";
        for (GlobalVar g : globalVars) {
            s += g.toString() + "\n";
        }
        for (Function f : functions) {
            s += f.toString() + "\n";
        }
        return s;
    }
}
