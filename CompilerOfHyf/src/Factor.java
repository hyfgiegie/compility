package src;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;

public class Factor {
    private String type;
    private Token token = null;
    private ArrayList<Factor> children = new ArrayList<>();

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

    public int getNumberOfChildren() {
        return children.size();
    }

    public int getNumberOfUnLeafChildren() {
        int cnt = 0;
        for (Factor factor : children) {
            if (!factor.type.equals("Token")) {
                cnt++;
            }
        }
        return cnt;
    }

    public Factor getLastChildren() {
        if (children.size() > 0) {
            return children.get(children.size() - 1);
        } else {
            return null;
        }
    }

    public boolean hasReturnSomething() {
        if (children.size() - 2 < 0) return false;
        Factor factor = children.get(children.size() - 2);
        ArrayList<Token> tokens = factor.toTokenList();
        if (tokens.size() > 2 && tokens.get(0).getValue().equals("return") && !tokens.get(1).getValue().equals(";")) {
            return true;
        }
        return false;
    }

    public boolean hasReturn() {
        if (children.size() - 2 < 0) return false;
        Factor factor = children.get(children.size() - 2);

        ArrayList<Token> tokens = factor.toTokenList();
        if (tokens.get(0).getValue().equals("return")) {
            return true;
        }
        return false;
    }

    //type is FuncRParams
    public boolean isLegalFuncRParams(Symbol funcFSymbol, SymbolTableStack tableStack) {
        ArrayList<Symbol> funcFParams = funcFSymbol.getOtherInfo().getParaTypes();
        for (int i = 0; i < funcFParams.size() && 2 * i < children.size(); i++) {
            Factor exp = children.get(2 * i);
            Symbol funcRPara = funcFParams.get(i);
            int dimension = exp.getDimension(tableStack);
            if (dimension != funcRPara.getDimension()) {
                return false;
            }
        }
        return true;
    }

    //this is exp
    public int getDimension(SymbolTableStack tableStack) {
        ArrayList<Token> tokens = this.toTokenList();

        if (!tokens.get(0).getType().equals("IDENFR")) return 0;

        Symbol symbol = tableStack.searchSymbol(tokens.get(0).getValue());
        if (symbol == null) return 0;//undefined

        int dimension = symbol.getDimension();

        if (tokens.size() == 1) return dimension;

        //defined symbol && size > 1
        int depth = 0;
        if (tokens.get(1).getValue().equals("[")) {
            for (int i = 1; i < tokens.size(); i++) {
                if (tokens.get(i).getValue().equals("[")) {
                    depth++;
                } else if (tokens.get(i).getValue().equals("]")) {
                    depth--;
                    if (depth == 0) {
                        dimension--;
                        if(dimension == 0) {
                            break;
                        }
                    }
                }
            }
        } else if (tokens.get(1).getValue().equals("(")) {
            Symbol symbol1 = tableStack.searchFuncSymbol(tokens.get(0).getValue());
            dimension = symbol1.getDimension();
        }

        return dimension;
    }

    public int getBeginLine() {
        ArrayList<Token> tokens = this.toTokenList();
        if (tokens.size() == 0) return -1;
        else {
            return tokens.get(0).getLines();
        }
    }

    public int getEndLine() {
        ArrayList<Token> tokens = this.toTokenList();
        if (tokens.size() == 0) return -1;
        else {
            return tokens.get(tokens.size() - 1).getLines();
        }
    }

    public Factor getWhichChild(int index) {
        if (index >= children.size()) return null;
        return children.get(index);
    }

    public ArrayList<Token> toTokenList() {
        ArrayList<Token> tokens = new ArrayList<>();
        if (type.equals("Token")) {
            tokens.add(token);
        } else {
            for (Factor factor : children) {
                if (factor == null) continue;
                tokens.addAll(factor.toTokenList());
            }
        }
        return tokens;
    }

}
