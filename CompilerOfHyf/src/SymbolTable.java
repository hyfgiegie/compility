package src;

import javax.swing.plaf.PanelUI;
import java.util.LinkedHashMap;

public class SymbolTable {
    //key: name
    private LinkedHashMap<String, Symbol> linkedHashMap = new LinkedHashMap<>();


    public SymbolTable() {
    }

    public boolean hasSymbol(String string) {
        return linkedHashMap.containsKey(string);
    }

    public void addSymbol(String name) {
        linkedHashMap.put(name, new Symbol(name));
    }

    public void addSymbol(Symbol symbol) {
        linkedHashMap.put(symbol.getName(), symbol);
    }

    public Symbol getSymbol(String name) {
        return linkedHashMap.get(name);
    }

}
