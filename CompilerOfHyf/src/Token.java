package src;

public class Token {
    private String type;
    private String value;
    private int lines;

    public Token(String type, String value, int lines) {
        this.type = type;
        this.value = value;
        this.lines = lines;
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
}
