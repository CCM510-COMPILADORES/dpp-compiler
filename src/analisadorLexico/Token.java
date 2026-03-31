package analisadorLexico;

public class Token {

    String tipo;
    InputTable ref;

    public Token(String tipo, InputTable input) {
        this.tipo = tipo;
        this.ref = input;
    }

    @Override
    public String toString() {
        return "<" + tipo + "," + ref + ">";
    }
}
