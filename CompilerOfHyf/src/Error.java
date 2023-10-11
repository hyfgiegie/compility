package src;

public class Error implements Comparable{
    private char code;
    private int lines;

    public Error(int lines, char code) {
        this.code = code;
        this.lines = lines;
    }

    @Override
    public String toString() {
        return lines + " " + code + "\n";
    }

    @Override
    public int compareTo(Object o) {
        if ((o instanceof Error) && lines > ((Error) o).lines) {
            return 1;
        } else if (lines == ((Error) o).lines) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Error
                && lines == ((Error) obj).lines
                && code == ((Error) obj).code;
    }
}
