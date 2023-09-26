package src;

import java.util.ArrayList;

public class Factor {
    private String type;
    private Token token = null;
    private ArrayList<Factor> children;

    public Factor(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public Factor(Token token) {
        this.type = "Token";
        this.token = token;
        this.children = new ArrayList<>();
    }

    public void addChild(Factor factor) {
        children.add(factor);
    }

    public void deleteChild() {
        children.remove(children.size() - 1);
    }

    public String getType() {
        return type;
    }

    public Token getToken() {
        return token;
    }

    public ArrayList<Factor> getChildren() {
        return children;
    }

}
