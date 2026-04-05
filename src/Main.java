package analisadorLexico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {

        // 1. Ler arquivo
        String codigo = Files.readString(Path.of("codigo.txt"));

        // 2. Criar iterator
        CharacterIterator it = new StringCharacterIterator(codigo);

        // 3. Tabela de símbolos
        LexTable lexTable = new LexTable();

        // 4. Lista de AFDs
        List<AFD> afds = List.of(
                new Number(lexTable),
                new Identifier(lexTable),
                new MathOperator(lexTable)
        );

        // 5. Lista de tokens
        List<Token> tokens = new ArrayList<>();

        // 6. Loop principal
        while (it.current() != CharacterIterator.DONE) {

            // Ignorar espaços
            if (Character.isWhitespace(it.current())) {
                it.next();
                continue;
            }

            Token token = null;

            // tenta cada AFD
            for (AFD afd : afds) {
                CharacterIterator backup = new StringCharacterIterator(codigo, it.getIndex());

                token = afd.evaluate(it);

                if (token != null) {
                    tokens.add(token);
                    break;
                } else {
                    // rollback se falhou
                    it = backup;
                }
            }

            // erro léxico
            if (token == null) {
                System.out.println("Erro léxico em: " + it.current());
                it.next();
            }
        }

        // 7. Imprimir tokens
        System.out.println("TOKENS:");
        tokens.forEach(System.out::println);

        // 8. Imprimir tabela de símbolos
        System.out.println("\nTABELA DE SÍMBOLOS:");
        lexTable.print();
    }
}