package src.ir.value.inst.terminal;

import src.ir.value.Value;
import src.ir.value.inst.Instruction;

public class BrInst extends Instruction {
    private Value cond;

    public void setCond(Value cond) {
        this.cond = cond;
    }

    @Override
    public String toString() {
        String s = "";
        if (cond == null) {
            s += "br label " + getOperands().get(0).getName();
        }
        else {
            s += "br " + cond.getType() + " " + cond.getName() + ", label " + getOperands().get(0).getName() +
                    ", label " + getOperands().get(1).getName();
        }
        return s;
    }
}
