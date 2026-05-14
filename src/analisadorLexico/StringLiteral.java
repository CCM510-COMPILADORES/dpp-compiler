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

            String tipo = isFormatadorString(lexema) ? "FORMAT_STRING" : "STR_LIT";
            return new Token(tipo, ref);
        }

        return null;
    }

    private boolean isFormatadorString(String lexema){

        //remove as aspas pra analisar se é formatador
        String conteudo = lexema.substring(1, lexema.length()-1).trim();

        if(conteudo.isEmpty()) return false;

        String[] partes = conteudo.split(",");
        for(String parte : partes){
            String p = parte.trim();
            if(!p.equals("%d") && !p.equals("%f") && !p.equals("%s")){
                return false;
            }
        }
        return true;
    }
}