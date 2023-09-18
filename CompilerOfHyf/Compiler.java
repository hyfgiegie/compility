import src.Lexer;
import src.Token;

import java.io.*;
import java.util.ArrayList;

public class Compiler {

    public static void main(String[] args) {
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        File srcFile = new File("testfile.txt");
        File dstFile = new File("output.txt");
        try {
            fileReader = new FileReader(srcFile);
            fileWriter = new FileWriter(dstFile);

            ArrayList<Integer> content = new ArrayList<>();

            int ch;
            while ((ch = fileReader.read()) != -1) {
                content.add(ch);
            }



            Lexer lexer = new Lexer(content);
            lexer.parseText();

            ArrayList<Token> tokens = lexer.getTokenList();

            for (int i = 0 ; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                if (i < tokens.size() - 1) {
                    fileWriter.write(token.getType() + ' ' + token.getValue() + '\n');
                } else {
                    fileWriter.write(token.getType() + ' ' + token.getValue());
                }
            }

            fileReader.close();
            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
