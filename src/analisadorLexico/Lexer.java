package analisadorLexico;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final CharacterIterator it;
    private final List<Token> tokens;
    private final List<AFD> afds;

    private static int line = 1;

    public Lexer(String code) {
        it = new StringCharacterIterator(code);
        this.tokens = new ArrayList<>();
        LexTable lexTable = new LexTable();

        this.afds = new ArrayList<>();
        afds.add(new MathOperator(lexTable));
        afds.add(new Number(lexTable));
        afds.add(new Identifier(lexTable));
    }

    public void skipWhiteSpaces(){
        while(Character.isWhitespace(it.current())) {
            if(it.current() == '\n'){
                line++;
            }
            it.next();
        }
    }

    public List<Token> getTokens(){
        Token t;
        do{
            skipWhiteSpaces();
            t = searchNextToken();
            if(t == null) error();
            t.setLine(line);
            tokens.add(t);
        }while(!t.tipo.equals("EOF"));
        return tokens;
    }

    private Token searchNextToken(){
        int pos = it.getIndex();
        for(AFD afd: afds){
            Token t = afd.evaluate(it);
            if(t != null) return t;
            it.setIndex(pos);
        }
        return null;
    }

    private void error(){
        throw new RuntimeException("Error: token not recognized!" +
                it.current());
    }



}
