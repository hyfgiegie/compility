package src.ir.value.inst.binary;

import src.ir.value.inst.Instruction;

public class MulInst extends Instruction {

    @Override
    public String toString() {
        return getName() + " = mul " + getType() + " " + getOperands().get(0).getName() + ", " + getOperands().get(1).getName();
    }
}
