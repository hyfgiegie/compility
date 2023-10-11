package src;

import java.util.ArrayList;

public class Symbol implements SymbolMacro {
    private String name;
    private int type;
    private int kind;
    private int dimension = 0;
    private Info otherInfo = new Info();

    public Symbol(String name, int type, int kind) {
        this.name = name;
        this.type = type;
        this.kind = kind;
    }

    public Symbol(String name) {
        this.name = name;
    }

    public Symbol() {
    }

    public int getType() {
        return type;
    }

    public int getKind() {
        return kind;
    }

    public Info getOtherInfo() {
        return otherInfo;
    }

    public void addPara(Symbol symbol) {
        otherInfo.addPara(symbol);
    }

    public int getNumOfFuncFParas() {
        return otherInfo.getParaNums();
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }

    public void addDimension() {
        this.dimension++;
    }

    public void setType(int type) {
        this.type = type;
        if (type == VOID) {
            dimension = -1;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public boolean isFunc() {
        return kind == FUNC;
    }
}
