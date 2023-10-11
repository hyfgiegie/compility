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
    private ArrayList<Error> errors = new ArrayList<>();

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

    public Lexer(String string) {
        ArrayList<Integer> content = new ArrayList<>();
        char[] chars = string.toCharArray();
        for (char ch : chars) {
            content.add((int) ch);
        }
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

    public void parseText() {
        skipWhite();
        while (parseUnit()) {
            skipWhite();
        }
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
            int num = parseString();
            if (tokenUnit.length() < 2) {
                //error
                errorContent = "There is an unknown signal " + tokenUnit.toString() + " in lines-" + lines;

            } else if (tokenUnit.charAt(tokenUnit.length() - 1) != '"') {
                errorContent = "There is an unfinished formatString in lines-" + lines;

            }
            tokenList.add(new Token("STRCON", tokenUnit.toString(), lines, num));
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
                    //means error of single | or &
                }
            }
        } else {
            //error of un-known signal
            errorContent = "There is an unknown signal " + (char) ch + " in lines-" + lines;

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

    private int parseString() {
        int cnt = 0;

        int ch = read();
        if (!isLegalChar((char) ch) && ch != '"') errors.add(new Error(lines, 'a'));
        int next = read();
        flashBack();
        if (ch == '%' && next == 'd') cnt++;

        while (ch != '"' && ch != -1) {
            tokenUnit.append((char) ch);

            ch = read();
            if (!isLegalChar((char) ch) && ch != '"') errors.add(new Error(lines, 'a'));
            next = read();
            flashBack();
            if (ch == '%' && next == 'd') cnt++;
        }

        if (ch == -1) {
            flashBack();
        } else {
            tokenUnit.append((char) ch);
        }
        return cnt;
    }

    private boolean isLegalChar(char ch) {
        //normal char
        if (ch == 32 || ch == 33 || (ch >= 40 && ch <= 126)) {
            if (ch == 92) {
                int next = read();
                flashBack();
                return next == 'n';
            } else {
                return true;
            }
        }

        if (ch == '%') {
            int next = read();
            flashBack();
            return next == 'd';
        }

        return false;
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

    public ArrayList<Error> getErrors() {
        return errors;
    }
}
