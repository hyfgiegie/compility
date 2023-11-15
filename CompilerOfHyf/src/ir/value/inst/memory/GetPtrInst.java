package src.ir.value.inst.memory;

import src.ir.type.Type;
import src.ir.type.single.PointerType;
import src.ir.type.single.SingleType;
import src.ir.value.Constant;
import src.ir.value.Value;
import src.ir.value.inst.Instruction;

import java.util.ArrayList;

public class GetPtrInst extends Instruction {
    public GetPtrInst() {
    }

    private ArrayList<Value> getOffsets() {
        ArrayList<Value> offsets = new ArrayList<>();
        for (int i = 1; i < getOperands().size(); i++) {
            offsets.add(getOperands().get(i));
        }
        return offsets;
    }

    @Override
    public String toString() {
        Value pointer = getOperands().get(0);
        SingleType pointTo = ((PointerType)pointer.getType()).getPointTo();
        String ret =  getName() + " = getelementptr " + pointTo + ", " + pointer.getType() + " " + pointer.getName();
        for (Value value : getOffsets()) {
            ret += ", " + value.getType() + " " + value.getName();
        }
        return ret;
    }
}
