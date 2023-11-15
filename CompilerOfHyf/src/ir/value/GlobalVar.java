package src.ir.value;

import src.ir.type.Type;
import src.ir.type.single.PointerType;

import java.util.ArrayList;

public class GlobalVar extends User {
    private boolean isConst;
    private Constant initValue;

    public GlobalVar(String name, Type type, boolean isConst) {
        super(name, type);
        this.isConst = isConst;
    }

    public void setInitValue(Constant initValue) {
        this.initValue = initValue;
    }

    public boolean isConst() {
        return isConst;
    }

    public Constant getInitValue() {
        return initValue;
    }

    @Override
    public String toString() {
        String constr = isConst ? "constant" : "global";
        String init = initValue == null ? ((PointerType)getType()).getPointTo() + " zeroinitializer" : initValue.toString();
        return getName() + " = dso_local " + constr + " " + init;
    }
}
