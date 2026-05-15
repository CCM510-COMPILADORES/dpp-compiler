import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import analisadorLexico.Lexer;
import analisadorLexico.Token;
import analisadorSintatico.Parser;

public class Main {

    public static void main(String[] args) throws IOException {

        // flags
        boolean printTokens = Arrays.asList(args).contains("-t");
        boolean printArvore = Arrays.asList(args).contains("-a");

        String code = Files.readString(Path.of("codigo.txt"));

        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.getTokens();

        if(printTokens){
            System.out.println("===== TOKENS =====");
            for(Token token : tokens){
                System.out.println(token);
            }
            System.out.println("==================\n");
        }

        Parser parser = new Parser(tokens, printArvore);
        if(parser.main()){
            parser.sucess();
            parser.salvarArquivo("saida.rs");
        } else {
            parser.error("Main");
        }
    }
}