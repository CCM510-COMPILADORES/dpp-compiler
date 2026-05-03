package analisadorLexico;

public class Token {

    protected String tipo;
    protected  InputTable ref;
    private int line;

    public Token(String tipo, InputTable input) {
        this.tipo = tipo;
        this.ref = input;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLexema(){
        return ref.getLexema();
    }

    public InputTable getRef() {
        return ref;
    }


    @Override
    public String toString() {
        return "<" + tipo + "," + line + "," + ref + ">";
    }
}
