package analisadorLexico;

import java.text.CharacterIterator;

public class Number extends AFD{

    public Number(LexTable lexTable) {
        super(lexTable);
    }

    @Override
    public Token evaluate(CharacterIterator code) {

        if(Character.isDigit(code.current())) {
            StringBuilder number = new StringBuilder();
            boolean isFloat = false;
            number.append(readNumber(code));

            if(code.current() == '.'){
                isFloat = true;
                number.append('.');
                code.next();

                if(!Character.isDigit(code.current())) {
                    //tratar erro futuro
                    return null;
                }
                number.append(readNumber(code));
            }
            if(isTokenSeparator(code)){
                String lexema = number.toString();
                InputTable ref = lexTable.add(lexema, 0);
                String type = isFloat ? "NUM_FLOAT" : "NUM_INT";
                return new Token(type, ref);
            }
        }
        return null;
    }

    private String readNumber(CharacterIterator code) {
        StringBuilder number = new StringBuilder();
        while(Character.isDigit(code.current())) {
            number.append(code.current());
            code.next();
        }
        return number.toString();
    }
}
