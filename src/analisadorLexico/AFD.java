package analisadorLexico;

import java.text.CharacterIterator;

public abstract class AFD {

    protected LexTable lexTable;

    public AFD(LexTable lexTable){
        this.lexTable = lexTable;
    }

    public abstract Token evaluate(CharacterIterator code);

    public boolean isTokenSeparator(CharacterIterator code){
        return code.current() == ' ' ||
                code.current() == '+' ||
                code.current() == '-' ||
                code.current() == '*' ||
                code.current() == '/' ||
                code.current() == '%' ||
                code.current() == '(' ||
                code.current() == ')' ||
                code.current() == '\n' ||
                code.current() == '\r' ||
                code.current() == CharacterIterator.DONE;
    }

}
