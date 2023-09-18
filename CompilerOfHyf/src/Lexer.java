package src;

import java.util.ArrayList;

public class Lexer {
    private StringBuffer tokenUnit;
    private int position;
    private int lines;
    private ArrayList<Integer> content;
    private int length;
    private ArrayList<Token> tokenList;
    private SignalTable signalTable;

    public Lexer(ArrayList<Integer> content) {
        this.tokenUnit = new StringBuffer();
        this.position = 0;
        this.lines = 0;
        this.content = content;
        this.length = content.size();
        this.tokenList = new ArrayList<>();
        this.signalTable = new SignalTable();
    }

    private void skipWhite() {
        if (position >= length) {
            return;
        }
        while (content.get(position) <= 32) {
            if (content.get(position) == '\n') {
                lines++;
            }
            position++;
            if (position >= length) {
                return;
            }
        }
    }

    private void skipThisLine() {
        if (position >= length) {
            return;
        }
        while (!(content.get(position) == 13 || content.get(position) == 10)) {
            position++;
            if (position >= length) {
                return;
            }
        }
    }

    private void skipMultiLines() {
        if (position >= length - 1) {
            return;
        }

        while (position < length - 1) {
            if (content.get(position) == '*' && content.get(position + 1) == '/') {
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

    //return false means end of text or error
    private boolean parseUnit() {
        int ch;
        tokenUnit = new StringBuffer();
        skipWhite();
        ch = read();
        tokenUnit.append((char) ch);
        if (ch == -1) {
            return false;
        } else if (isLetter(ch) || ch == '_') {
            parseIdent();
            String type = signalTable.getSym(tokenUnit.toString());
            if (type == null) {
                tokenList.add(new Token("IDENFR", tokenUnit.toString()));
            } else {
                tokenList.add(new Token(type, tokenUnit.toString()));
            }
        } else if (isNum(ch)) {
            parseNum();
            tokenList.add(new Token("INTCON", tokenUnit.toString()));
        } else if (ch == '"') {
            parseString();
            if (tokenUnit.charAt(tokenUnit.length() - 1) != '"' || tokenUnit.length() < 2) {
                //error
                return false;
            }
            tokenList.add(new Token("STRCON", tokenUnit.toString()));
        } else {
            parseSignal();
            String type = signalTable.getSym(tokenUnit.toString());
            if (type == null) {
                if (tokenUnit.toString().equals("//") || tokenUnit.toString().equals("/*")) {
                    return true;
                } else {
                    //error
                    return false;
                }
            }
            tokenList.add(new Token(type, tokenUnit.toString()));
        }
        return true;
    }

    private int read() {
        position++;
        if (position <= length) {
            return content.get(position - 1);
        } else {
            return -1;
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
                    System.out.println("Error signal of single '&' in lines-\n" + lines);
                }
                break;
            case '|':
                if ((ch = read()) == '|') {
                    tokenUnit.append((char) ch);
                } else {
                    flashBack();
                    //error
                    System.out.println("Error signal of single '|' in lines-\n" + lines);
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
                break;
            default:
                System.out.println("There is an un-known signal-\n" + (char) ch + " in lines-" + lines);
                break;
        }
    }

    private void parseString() {
        int ch = read();
        while (ch != '"' && ch != -1) {
            tokenUnit.append((char) ch);
            ch = read();
        }
        if (ch == -1) {
            flashBack();
            System.out.println("There is an un-finished string in lines-\n" + lines);
        } else {
            tokenUnit.append((char) ch);
        }
    }

    private void parseNum() {
        int ch = read();
        while (isNum(ch)) {
            tokenUnit.append((char) ch);
            ch = read();
        }
        flashBack();
    }

    private void parseIdent() {
        int ch = read();

        while (isNum(ch) || isLetter(ch) || ch == '_') {
            tokenUnit.append((char) ch);
            ch = read();
        }
        flashBack();
    }

    private boolean isNum(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        return false;
    }

    private boolean isLetter(int ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return true;
        }
        if (ch >= 'a' && ch <= 'z') {
            return true;
        }
        return false;
    }

    public void parseText() {
        skipWhite();
        while (parseUnit()) {
            skipWhite();
        }
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }
}
