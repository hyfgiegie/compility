package src.ir.value.inst.memory;

import src.ir.value.Value;
import src.ir.value.inst.Instruction;

public class LoadInst extends Instruction {

    @Override
    public String toString() {
        Value pointer = getOperands().get(0);
        return getName() + " = load " + getType() + ", " + pointer.getType() + " " + pointer.getName();
    }
}
