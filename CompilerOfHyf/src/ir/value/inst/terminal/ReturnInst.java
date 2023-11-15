package src.ir.value.inst.terminal;

import src.ir.value.Value;
import src.ir.value.inst.Instruction;

public class ReturnInst extends Instruction {


    @Override
    public String toString() {
        if (getOperands() != null && getOperands().size() == 1) {
            Value value = getOperands().get(0);
            return "ret " + value.getType() + " " + value.getName();
        }
        else {
            return "ret void";
        }
    }
}
