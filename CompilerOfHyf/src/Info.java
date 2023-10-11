package src;

import java.util.ArrayList;

public class Info {
    private ArrayList<Symbol> paraTypes = new ArrayList<>();

    public Info() {
    }

    public int getParaNums() {
        return paraTypes.size();
    }

    public ArrayList<Symbol> getParaTypes() {
        return paraTypes;
    }

    public void addPara(Symbol symbol) {
        paraTypes.add(symbol);
    }
}
