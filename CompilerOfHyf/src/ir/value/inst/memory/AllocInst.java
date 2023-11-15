package src.ir.value.inst.memory;

import src.ir.type.single.PointerType;
import src.ir.value.inst.Instruction;

public class AllocInst extends Instruction {

    @Override
    public String toString() {
        return getName() + " = alloca " + ((PointerType)getType()).getPointTo();
    }
}
