package src.ir.value.inst.other;

import src.ir.value.inst.Instruction;

public class IcmpInst extends Instruction {

    private String cond;

    public void setCond(String s) {
        cond = s;
    }

    @Override
    public String toString() {
        return getName() + " = icmp " + cond + " " + getOperands().get(0).getType() +
                " " + getOperands().get(0).getName() + ", " + getOperands().get(1).getName();
    }
}
