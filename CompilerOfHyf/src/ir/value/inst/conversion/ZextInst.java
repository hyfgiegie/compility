package src.ir.value.inst.conversion;

import src.ir.type.Type;
import src.ir.value.inst.Instruction;

public class ZextInst extends Instruction {
    private Type toType;

    public void setToType(Type toType) {
        this.toType = toType;
    }

    @Override
    public String toString() {
        return getName() + " = zext " + getOperands().get(0).getType() + " " + getOperands().get(0).getName() +
                " to " + toType;
    }
}
