package src.ir.value;

import src.ir.type.LabelType;
import src.ir.type.Type;
import src.ir.value.inst.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions;

    public BasicBlock() {
        newName();
        setType(new LabelType());
        instructions = new ArrayList<>();
    }

    public BasicBlock(String name, Type type) {
        super(name, type);
        instructions = new ArrayList<>();
    }

    public void addInstruction(Instruction inst) {
        instructions.add(inst);
    }

    public Instruction getLastInstruction() {
        if (instructions != null && instructions.size() > 0) {
            return instructions.get(instructions.size() - 1);
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        String s = "";
        if (getName() != null) {
            String name = getName();
            char[] chars = name.toCharArray();
            String v = String.valueOf(chars, 1, chars.length - 1);
            s += v + ":\n";
        }
        for (Instruction instruction : instructions) {
            s += "  " + instruction.toString() + "\n";
        }
        return s;
    }
}
