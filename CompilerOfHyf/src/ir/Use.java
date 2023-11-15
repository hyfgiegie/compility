package src.ir;

import src.ir.value.User;
import src.ir.value.Value;

public class Use {
    private Value value;
    private User user;

    //表示该 value 是 user 的第几个操作数
    private int pos;

    public Use(Value value, User user, int pos) {
        this.user = user;
        this.value = value;
        this.pos = pos;
    }

    public User getUser() {
        return user;
    }

    public Value getValue() {
        return value;
    }

    public int getPos() {
        return pos;
    }
}
