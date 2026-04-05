package analisadorLexico;

import java.text.CharacterIterator;

public class Identifier extends AFD{

    public Identifier(LexTable lexTable) {
        super(lexTable);
    }

    @Override
    public Token evaluate(CharacterIterator code) {
        if(Character.isLetter(code.current()) || code.current() == '_') {

            StringBuilder lexema = new StringBuilder();
            lexema.append(code.current());
            code.next();
            while(Character.isLetterOrDigit(code.current()) || code.current() == '_') {
                lexema.append(code.current());
                code.next();
            }

            String palavra = lexema.toString();

            String tipo;
            if (KeyWords.isKeyWord(palavra)) {
                tipo = KeyWords.getTokenType(palavra);
            } else {
                tipo = "ID";
            }

            InputTable ref = lexTable.add(palavra,0);
            return new Token(tipo, ref);

        }
        return null;
    }
}
