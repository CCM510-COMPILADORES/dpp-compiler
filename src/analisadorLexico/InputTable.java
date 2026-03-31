package analisadorLexico;

public class InputTable {

    private String lexema;
    private int linha;

    public InputTable(String lexema, int linha) {
        this.lexema = lexema;
        this.linha = linha;
    }

    @Override
    public String toString() {
        return lexema + "(linha " + linha + ")";
    }
}
