package src.ir.value.inst;

import src.ir.type.Type;
import src.ir.value.User;

public class Instruction extends User {
    public Instruction() {
    }

    public Instruction(String name, Type type) {
        super(name, type);
    }
}
