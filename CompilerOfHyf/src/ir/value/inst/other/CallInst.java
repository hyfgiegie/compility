package src.ir.value.inst.other;

import src.ir.type.Type;
import src.ir.type.single.ArrayType;
import src.ir.type.single.PointerType;
import src.ir.type.single.SingleType;
import src.ir.value.Function;
import src.ir.value.Value;
import src.ir.value.inst.Instruction;

import java.util.ArrayList;

public class CallInst extends Instruction {

    private ArrayList<Value> getFuncRParams() {
        if (getOperands().size() == 1) {
            return null;
        }
        else {
            ArrayList<Value> values = new ArrayList<>();
            for (int i = 1; i < getOperands().size(); i++) {
                values.add(getOperands().get(i));
            }
            return values;
        }
    }

    private Function getFunction() {
        return (Function) getOperands().get(0);
    }

    @Override
    public String toString() {
        String s = "";
        if (getName() != null) {
            s += getName() + " = ";
        }
        s += "call " + getFunction().getType() + " " + getFunction().getName();

        //real params (new PointerType((SingleType) ((ArrayType) value.getType()).getInnerType()))
        s += "(";
        if (getFuncRParams() != null) {
            for (int i = 0; i < getFuncRParams().size(); i++) {
                Value value = getFuncRParams().get(i);
                if (i == getFuncRParams().size() - 1) {
                    s += value.getType() + " " + value.getName();
                }
                else {
                    s += value.getType() + " " + value.getName() + ", ";
                }
            }
        }
        s += ")";
        return s;
    }
}
