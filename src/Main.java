import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import analisadorLexico.Lexer;
import analisadorLexico.Token;

public class Main {

    public static void main(String[] args) throws IOException {

        String code = Files.readString(Path.of("codigo.txt"));

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.getTokens();

        for(Token token : tokens){
            System.out.println(token);
        }
    }
}