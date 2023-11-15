package src.frontend;

import java.util.HashMap;

public class SignalTable {
    private static final HashMap<String, String> sigTable = new HashMap<>();

    public SignalTable() {
        sigTable.put("!", "NOT");
        sigTable.put("&&", "AND");
        sigTable.put("||", "OR");
        sigTable.put("+", "PLUS");
        sigTable.put("-", "MINU");
        sigTable.put("*", "MULT");
        sigTable.put("/", "DIV");
        sigTable.put("%", "MOD");
        sigTable.put("<", "LSS");
        sigTable.put("<=", "LEQ");
        sigTable.put(">", "GRE");
        sigTable.put(">=", "GEQ");
        sigTable.put("==", "EQL");
        sigTable.put("!=", "NEQ");
        sigTable.put("=", "ASSIGN");
        sigTable.put(";", "SEMICN");
        sigTable.put(",", "COMMA");
        sigTable.put("(", "LPARENT");
        sigTable.put(")", "RPARENT");
        sigTable.put("[", "LBRACK");
        sigTable.put("]", "RBRACK");
        sigTable.put("{", "LBRACE");
        sigTable.put("}", "RBRACE");
        sigTable.put("main", "MAINTK");
        sigTable.put("const", "CONSTTK");
        sigTable.put("int", "INTTK");
        sigTable.put("break", "BREAKTK");
        sigTable.put("continue", "CONTINUETK");
        sigTable.put("if", "IFTK");
        sigTable.put("else", "ELSETK");
        sigTable.put("for", "FORTK");
        sigTable.put("getint", "GETINTTK");
        sigTable.put("printf", "PRINTFTK");
        sigTable.put("return", "RETURNTK");
        sigTable.put("void", "VOIDTK");
    }

    public String getSym(String buf) {
        if (sigTable.containsKey(buf)) {
            return sigTable.get(buf);
        } else {
            return null;
        }
    }

}
