package src.ir.value.inst.memory;

import src.ir.value.Value;
import src.ir.value.inst.Instruction;

public class StoreInst extends Instruction {

    @Override
    public String toString() {
        Value operand1 = getOperands().get(0);
        Value operand2 = getOperands().get(1);
        return "store " + operand1.getType() + " " + operand1.getName() + ", " + operand2.getType() + " " + operand2.getName();
    }
}
