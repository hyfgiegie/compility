package src.ir.value;

import src.ir.type.Type;

import java.util.ArrayList;

public class Function extends Value {
    private ArrayList<BasicBlock> basicBlocks;

    private ArrayList<Value> funcFParams;

    public Function(String name, Type type) {
        super(name, type);
        basicBlocks = new ArrayList<>();
        basicBlocks.add(new BasicBlock());
        funcFParams = new ArrayList<>();
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public void addFuncFParam(Value value) {
        funcFParams.add(value);
    }

    @Override
    public String toString() {
        String s = "";
        s += "define dso_local " + getType() + " " + getName();
        //param
        s += "(";
        for (int i = 0 ; i < funcFParams.size(); i++) {
            if (i == funcFParams.size() - 1) {
                s += funcFParams.get(i).getType() + " " + funcFParams.get(i).getName();
            }
            else {
                s += funcFParams.get(i).getType() + " " + funcFParams.get(i).getName() + ", ";
            }
        }
        s += ")";
        // block
        s += " {\n";
        for (BasicBlock basicBlock : basicBlocks) {
            s += basicBlock.toString();
        }
        s += "}\n";
        return s;
    }
}
