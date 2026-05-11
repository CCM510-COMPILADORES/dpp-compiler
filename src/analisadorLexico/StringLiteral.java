package analisadorLexico;

import java.text.CharacterIterator;

public class StringLiteral extends AFD {

    public StringLiteral(LexTable lexTable) {
        super(lexTable);
    }

    @Override
    public Token evaluate(CharacterIterator code) {

        if(code.current() != '"') return null;

        StringBuilder string = new StringBuilder();
        string.append(code.current()); // consome a aspa inicial
        code.next();

        while(code.current() != '"' && code.current() != CharacterIterator.DONE) {
            string.append(code.current());
            code.next();
        }

        if(code.current() == CharacterIterator.DONE) {
            throw new RuntimeException("Erro léxico: string não fechada!");
        }

        string.append(code.current()); // consome a aspa final
        code.next();

        if(isTokenSeparator(code)) {
            String lexema = string.toString();
            InputTable ref = lexTable.add(lexema, 0);
            return new Token("STRING", ref);
        }

        return null;
    }
}