package src.ir.value;

import src.ir.type.Type;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands;

    public User() {
        operands = new ArrayList<>();
    }

    public User(String name, Type type) {
        super(name, type);
        operands = new ArrayList<>();
    }

    public void addOperands(Value value) {
        operands.add(value);
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }

}
