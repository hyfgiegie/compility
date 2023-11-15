package src.frontend;

public class Token {
    private String type;
    private String value;
    private int lines;
    private int numOfFormatChars;

    public Token(String type, String value, int lines) {
        this.type = type;
        this.value = value;
        this.lines = lines;
    }

    public Token(String type, String value, int lines, int numOfFormatChars) {
        this.type = type;
        this.value = value;
        this.lines = lines;
        this.numOfFormatChars = numOfFormatChars;
    }

    public int getLines() {
        return lines;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getNumOfFormatChars() {
        return numOfFormatChars;
    }
}
