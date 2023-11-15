package src.ir;

import src.ir.value.Function;
import src.ir.value.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class ValueTable {
    private ArrayList<HashMap<String, Value>> symTbls;
    private static ValueTable instance;


    private ValueTable() {
        symTbls = new ArrayList<>();
    }

    public static ValueTable getInstance() {
        if (instance == null) {
            instance = new ValueTable();
        }
        return instance;
    }

    public Value find(String ident) {
        int len = symTbls.size() - 1;
        while (len >= 0) {
            if (symTbls.get(len).containsKey(ident)) {
                return symTbls.get(len).get(ident);
            }
            len--;
        }
        return null;
    }

    public Function findFunc(String ident) {
        if (symTbls.get(0).containsKey(ident)) {
            Value value = symTbls.get(0).get(ident);
            if (value instanceof Function) {
                return (Function) value;
            }
        }
        return null;
    }

    public void pushSymbol(String ident, Value value) {
        HashMap<String, Value> symTbl = symTbls.get(symTbls.size() - 1);
        symTbl.put(ident, value);
    }

    public void pushSymTbl() {
        HashMap<String, Value> symTbl = new HashMap<>();
        symTbls.add(symTbl);
    }

    public void popSymTbl() {
        symTbls.remove(symTbls.size() - 1);
    }

}
