package src;

import java.util.ArrayList;
import java.util.Stack;

public class SymbolTableStack {
    private ArrayList<SymbolTable> symbolTables = new ArrayList<>();

    public SymbolTableStack() {}

    public void push(SymbolTable symbolTable) {
        symbolTables.add(symbolTable);
    }

    public SymbolTable pop() {
        SymbolTable symbolTable = symbolTables.get(symbolTables.size() - 1);
        symbolTables.remove(symbolTables.size() - 1);
        return symbolTable;
    }

    public SymbolTable peek() {
        return symbolTables.get(symbolTables.size() - 1);
    }

    public Symbol searchSymbol(String string) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            if (symbolTables.get(i).hasSymbol(string)) {
                return symbolTables.get(i).getSymbol(string);
            }
        }
        return null;
    }

    public Symbol searchFuncSymbol(String name) {
        SymbolTable symbolTable = symbolTables.get(0);
        if (symbolTable.hasSymbol(name)) {
            Symbol symbol = symbolTable.getSymbol(name);
            if (symbol.isFunc()) {
                return symbol;
            }
        }
        return null;
    }

    public boolean isReDefined(String name) {
        return peek().hasSymbol(name);
    }
}
