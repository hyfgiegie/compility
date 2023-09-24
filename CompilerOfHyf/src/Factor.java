package src;

import java.util.ArrayList;

public class Factor {
    private String type;
    private Token token = null;
    private final ArrayList<Factor> children;

    public Factor(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(Factor factor) {
        children.add(factor);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public ArrayList<Factor> getChildren() {
        return children;
    }
}
