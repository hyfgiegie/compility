package src.ir.value;

import src.ir.Use;
import src.ir.type.Type;

import java.util.ArrayList;

public class Value {
    private String name;
    private Type type;
    private ArrayList<Use> useList = new ArrayList<>();
    private static int valNumber = -1;
    private boolean isFuncParam = false;

    public Value() {
    }

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void addUse(Use use) {
        useList.add(use);
    }

    public ArrayList<Use> getUseList() {
        return useList;
    }

    //who uses this value
    public ArrayList<User> getUserList() {
        ArrayList<User> userList = new ArrayList<>();
        for (Use use : useList) {
            if (!userList.contains(use.getUser())) {
                userList.add(use.getUser());
            }
        }
        return userList;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void newName() {
        name = "%var" + ++valNumber;
    }

    public void isFuncParam() {
        isFuncParam = true;
    }

    public boolean getIsFuncParam() {
        return isFuncParam;
    }
}
