package src.ir.type.single;

import src.ir.type.Type;

public class PointerType extends SingleType {
    private SingleType pointTo;

    public PointerType(SingleType pointTo) {
        this.pointTo = pointTo;
    }

    public SingleType getPointTo() {
        return pointTo;
    }

    @Override
    public String toString() {
        return pointTo.toString() + "*";
    }
}
