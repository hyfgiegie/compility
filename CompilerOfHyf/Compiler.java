import src.frontend.*;
import src.frontend.Error;
import src.ir.IRModule;
import src.ir.Visitor;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

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
            lexer.parseText();

            File dstFile = new File("output.txt");
            fileWriter = new FileWriter(dstFile);
            ArrayList<Token> tokens = lexer.getTokenList();

            Parser parser = new Parser(tokens);
            Factor root = parser.parse();
            output(root, fileWriter);
            fileWriter.close();


            File errorFile = new File("error.txt");
            fileWriter = new FileWriter(errorFile);

            ArrayList<Error> errors = lexer.getErrors();
            errors.addAll(parser.getErrors());
            outErrors(errors, fileWriter);
            fileWriter.close();

            File irFile = new File("llvm_ir.txt");
            fileWriter = new FileWriter(irFile);
            Visitor visitor = new Visitor(root);
            visitor.visitCompUnit();
            fileWriter.write(IRModule.getInstance().toString());

            fileReader.close();
            fileWriter.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void output(Factor factor, FileWriter fileWriter) {
        if (factor == null) return;
        for (int i = 0; i < factor.getChildren().size(); i++) {
            output(factor.getChildren().get(i), fileWriter);
        }
        if (factor.getType().equals("Token")) {
            try {
                fileWriter.write(factor.getToken().getType() + " " + factor.getToken().getValue() + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (!factor.getType().equals("BlockItem")
                && !factor.getType().equals("Decl")
                && !factor.getType().equals("BType")) {
            try {
                fileWriter.write("<" + factor.getType() + ">" + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void outErrors(ArrayList<Error> errors, FileWriter fileWriter) {

        //sort errors by error.lines
        Collections.sort(errors);

        //output errors
        for (Error error : errors) {
            try {
                fileWriter.write(error.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
