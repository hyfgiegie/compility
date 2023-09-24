import src.Lexer;
import src.Token;

import java.io.*;
import java.util.ArrayList;

public class Compiler {

    public static void main(String[] args) {
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        File srcFile = new File("testfile.txt");
        try {
            fileReader = new FileReader(srcFile);
            ArrayList<Integer> content = new ArrayList<>();
            int ch;
            while ((ch = fileReader.read()) != -1) {
                content.add(ch);
            }

            Lexer lexer = new Lexer(content);
            boolean result = lexer.parseText();
            if (result) {
                File dstFile = new File("output.txt");
                fileWriter = new FileWriter(dstFile);
                ArrayList<Token> tokens = lexer.getTokenList();

            } else {
                File errorFile = new File("error.txt");
                fileWriter = new FileWriter(errorFile);
                fileWriter.write(lexer.getErrorContent());
            }

            fileReader.close();
            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
