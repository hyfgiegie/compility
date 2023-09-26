package src;

import java.util.ArrayList;

public class Lexer {
    private StringBuffer tokenUnit;
    private int position;
    private int lines;
    private final ArrayList<Integer> content;
    private final int length;
    private final ArrayList<Token> tokenList;
    private final SignalTable signalTable;

    private String errorContent;

    public Lexer(ArrayList<Integer> content) {
        this.tokenUnit = new StringBuffer();
        this.position = 0;
        this.lines = 1;
        this.content = content;
        this.length = content.size();
        this.tokenList = new ArrayList<>();
        this.signalTable = new SignalTable();
        this.errorContent = "";
    }

    private void skipWhite() {
        while (position < length &&
                content.get(position) <= 32) {
            if (content.get(position) == '\n') lines++;
            position++;
        }
    }

    private void skipThisLine() {
        while (position < length &&
                content.get(position) != 13 &&
                content.get(position) != 10) {
            position++;
        }
    }

    private void skipMultiLines() {
        while (position < length - 1) {
            if (content.get(position) == '*' &&
                    content.get(position + 1) == '/') {
                break;
            }
            if (content.get(position) == '\n') lines++;
            position++;
        }
        position += 2;
    }

    private void flashBack() {
        position--;
    }

    private int read() {
        if (position < length) {
            return content.get(position++);
        } else {
            return -1;
        }
    }

    private boolean isNum(int ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isLetter(int ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return true;
        }
        return ch >= 'a' && ch <= 'z';
    }

    private boolean isLegalSig(int ch) {
        String normal = "!<>=&|/+-*%;,()[]{}";
        return normal.indexOf((char) ch) != -1;
    }

    public boolean parseText() {
        skipWhite();
        while (parseUnit()) {
            skipWhite();
        }
        return errorContent.length() == 0;
    }

    //return false means end of text or error
    private boolean parseUnit() {
        skipWhite();
        tokenUnit = new StringBuffer();

        int ch = read();
        if (ch == -1) {
            //end
            return false;
        } else if (isLetter(ch) || ch == '_') {
            tokenUnit.append((char) ch);
            parseIdent();
            String type = signalTable.getSym(tokenUnit.toString());
            if (type == null) tokenList.add(new Token("IDENFR", tokenUnit.toString(), lines));
            else tokenList.add(new Token(type, tokenUnit.toString(), lines));
        } else if (isNum(ch)) {
            tokenUnit.append((char) ch);
            parseNum();
            tokenList.add(new Token("INTCON", tokenUnit.toString(), lines));
        } else if (ch == '"') {
            tokenUnit.append((char) ch);
            parseString();
            if (tokenUnit.length() < 2) {
                //error
                errorContent = "There is an unknown signal " + tokenUnit.toString() + " in lines-" + lines;
                return false;
            } else if (tokenUnit.charAt(tokenUnit.length() - 1) != '"') {
                errorContent = "There is an unfinished formatString in lines-" + lines;
                return false;
            }
            tokenList.add(new Token("STRCON", tokenUnit.toString(), lines));
        } else if (isLegalSig(ch)) {
            tokenUnit.append((char) ch);
            parseSignal();
            String type = signalTable.getSym(tokenUnit.toString());

            //not comments
            if (!tokenUnit.toString().equals("//") &&
                    !tokenUnit.toString().equals("/*")) {
                if (type != null) tokenList.add(new Token(type, tokenUnit.toString(), lines));
                else {
                    errorContent = "There is an unknown signal " + tokenUnit.toString() + " in lines-" + lines;
                    return false;//means error of single | or &
                }
            }
        } else {
            //error of un-known signal
            errorContent = "There is an unknown signal " + (char) ch + " in lines-" + lines;
            return false;
        }
        return true;
    }

    private void parseIdent() {
        int ch = read();
        while (isNum(ch) || isLetter(ch) || ch == '_') {
            tokenUnit.append((char) ch);
            ch = read();
        }
        flashBack();
    }

    private void parseNum() {
        int ch = read();
        while (isNum(ch)) {
            tokenUnit.append((char) ch);
            ch = read();
        }
        flashBack();
    }

    private void parseString() {
        int ch = read();
        while (ch != '"' && ch != -1) {
            tokenUnit.append((char) ch);
            ch = read();
        }
        if (ch == -1) {
            flashBack();
        } else {
            tokenUnit.append((char) ch);
        }
    }

    private void parseSignal() {
        int ch = tokenUnit.charAt(0);
        switch (ch) {
            case '!':
            case '<':
            case '>':
            case '=':
                if ((ch = read()) == '=') {
                    tokenUnit.append((char) ch);
                } else {
                    flashBack();
                }
                break;
            case '&':
                if ((ch = read()) == '&') {
                    tokenUnit.append((char) ch);
                } else {
                    flashBack();
                    //error
                }
                break;
            case '|':
                if ((ch = read()) == '|') {
                    tokenUnit.append((char) ch);
                } else {
                    flashBack();
                    //error
                }
                break;
            case '/':
                ch = read();
                if (ch == '/') {
                    tokenUnit.append((char) ch);
                    skipThisLine();
                } else if (ch == '*') {
                    tokenUnit.append((char) ch);
                    skipMultiLines();
                } else {
                    flashBack();
                }
                break;
            case '+':
            case '-':
            case '*':
            case '%':
            case ';':
            case ',':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            default:
                break;
        }
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }

    public String getErrorContent() {
        return errorContent;
    }
}
