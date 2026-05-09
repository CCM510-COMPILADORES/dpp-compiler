package analisadorSintatico;

import analisadorLexico.InputTable;
import analisadorLexico.LexTable;
import analisadorLexico.Token;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    List<Token> tokens;
    Token token;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ====== FUNÇÕES UTILITÁRIAS ======//

    public Token getNextToken(){
        if(!tokens.isEmpty())
            return tokens.removeFirst();
        return null;
    }

    //Serve para casos onde há mais de uma definição da regra. Evita quebrar no meio da validação
    public Token peek(){
        if(!tokens.isEmpty())
            return tokens.getFirst();
        return null;
    }

    public boolean isOpArit(){
        String l = peek().getLexema();
        return l.equals("+") || l.equals("-")
                || l.equals("*") || l.equals("/")
                || l.equals("%");
    }

    public boolean isOpLogic(){
        String l = peek().getLexema();
        return l.equals("&&") || l.equals("||");
    }

    public boolean isOpComp(){
        String l = peek().getLexema();
        return l.equals("==") || l.equals("!=")
                || l.equals("<=") || l.equals(">=")
                || l.equals(">") || l.equals("<");
    }

    public boolean isFormatador(){
        String l = peek().getLexema();
        return l.equals("%d") || l.equals("%f") || l.equals("%s");
    }

    private boolean temComando(){
        // espia se o próximo token pode iniciar um comando
        String l = peek().getLexema();
        return l.equals("houdini") || l.equals("problems") ||
                l.equals("catapult") || l.equals("pleaser") || ... ;
    }

    private boolean temExpressao(){
        // espia se o próximo token pode iniciar uma expressão
        String t = peek().getTipo();
        String l = peek().getLexema();
        return t.equals("id") || t.equals("int") ||
                t.equals("float") || l.equals("(") || ... ;
    }

    public void error(String regra){
        System.out.println("Erro!");
        System.out.println("=============");
        System.out.println("Regra: " + regra);
        System.out.println("Token inválido: ");
        System.out.println("=============");
    }

    public void sucess(){
        System.out.println("Sucesso! Todos os tokens são válidos");
    }

    /*
    *
    * palavra    → [a-zA-Z]+[a-zA-Z]*
    num        → [0-9]+


    int        → num+
    float      → num+ '.' num+
    string     → `“`palavra*`”`

    tipo       → `space` | `lithium` | `judas`
    atribuicao → tipo id `=` expressao `;`| id `=` expressao `;`
    declaracao → tipo id `;`

    comentario → `~~` palavra* `\n`

    expressao  → termo expressao'
    expressao' → op_arit termo expressao' | ε
    termo      → `(` expressao `)` | int | float | string | id | chamada_funcao

    condicao   → termo condicao'
    condicao'  → (op_logic | op_comp) termo condicao' | ε
    termo      → `(` condicao `)`  | int | float | id

    saida      → `catapult` `[` expressao `]` `;`

    entrada           → `pleaser`  `[` entrada' `]` `;`
    entrada'          → `“` ponteiros `”` `,` identificadores
    ponteiros         → formatadores ponteiros'
    ponteiros'        → `,` ponteiros | ε
    identificadores   → id identificadores'
    identificadores'  → `,` identificadores | ε
    formatadores      → `%d` | `%f` | `%s`


    declaracao_funcao       → tipo `PREY` id `[` parametros_declaracao `]` `{` comando*  `HOMETOWN` expressao? `}`
    parametros_declaracao   → tipo id parametros_declaracao' | ε
    parametros_declaracao'  → `,` tipo id parametros_declaracao' | ε

    chamada_funcao          → id `[` parametros_chamada `]` `;`
    parametros_chamada      → expressao parametros_chamada'
    parametros_chamada'     → `,` expressao parametros_chamada' | ε

    if        → `houdini`  `(` condicao `)` `{` comando* `}` else
    else      → `more` `{` comando* `}` | ε
    while     → `problems` `(` condicao `)` `{` comando* `}`
    do_while  → `not...ok` `{` comando* `}` `while` `(` condicao `)` `;`
    for       → `bloomfield` `(` atribuicao condicao atribuicao `)` `{` comando* `}`

    comando   → comentario | atribuicao | entrada | saida | if | while | do_while | for | chamada_funcao
    main      → declaracao_funcao* `style` codigo `borderline`
    codigo    → comando* codigo* | ε
    * */

    private boolean id(){
        return matchT("id");
    }

    private boolean op_arit(){
        return matchL("+") || matchL("-") ||matchL("*") ||  matchL("/")
                || matchL("%");
    }

    private boolean op_comp(){
        return matchL("==") || matchL("!=") || matchL(">=") || matchL("<=")
                || matchL(">") || matchL("<");
    }

    private boolean op_logic(){
        return matchL("&&") || matchL("||");
    }

    private boolean tipo(){
        return matchL("int") || matchL("float") || matchL("string");
    }

    private boolean atribuicao(){
        if(peek() != null && peek().getTipo().equals("tipo")){
            return tipo() && id() && matchL("=") && expressao() && matchL(";");
        }
        return (id() && matchL("=") && expressao() && matchL(";"));
    }

    private boolean declaracao(){
        return tipo() && id() && matchL(";");
    }

    private boolean comentario(){
        return matchL("~~") && palavra() && matchL("\n");
    }

    private boolean expressao(){
        return termo() && expressaoLinha();
    }

    private boolean expressaoLinha(){
        if(peek() != null && isOpArit()){
            return op_arit() && termo() && expressaoLinha();
        }
        //epslon
        return true;
    }

    private boolean termo(){
        if(peek() != null && peek().getLexema().equals("(")) {
            return matchL("(") && expressao() && matchT(")");
        }
        return matchT("int") || matchT("float") || matchT("string") || matchT("id");
    }

    private boolean condicao(){
        return termo() && condicaoLinha();
    }

    private boolean condicaoLinha(){

        if(peek() != null && (isOpLogic() || isOpComp())){
            return (op_arit() || op_comp()) && termo() && condicaoLinha();
        }
        //epslon
        return true;
    }

    private boolean saida(){
        return matchL("catapult") && matchL("[") && expressao() && matchL("]");
    }

    private boolean entrada(){
        return matchL("pleaser") && matchL("[") && entradaLinha() && matchL("]");
    }

    private boolean entradaLinha(){
        return matchL("\"") && ponteiros() && matchL("\"") && matchL(",") && identificadores();
    }

    private boolean ponteiros(){
        return formatadores() && ponteirosLinha();
    }

    private boolean ponteirosLinha(){
        if(peek() != null && peek().getTipo().equals(",")){
            return matchL(",") && ponteiros();
        }
        //epslon
        return true;
    }

    private boolean identificadores(){
        return id() && identificadoresLinha();
    }

    private boolean identificadoresLinha(){
        if(peek() != null && peek().getTipo().equals(",")){
            return matchL(",") && identificadores();
        }
        //epslon
        return true;
    }

    private boolean formatadores(){
        if(peek() != null && isFormatador()){
            return matchL("%d") || matchL("%f") || matchL("%s");
        }
        return false;
    }

    private boolean declaracaoFuncao(){
        if(!tipo() || !matchL("PREY") || !id() || !matchL("[") ||
                !parametrosDeclaracao() || !matchL("]") || !matchL("{"))
            return false;

        // comando* → zero ou mais, fica consumindo enquanto tiver comando
        while(temComando()){
            if(!comando()) return false;
        }

        if(!matchL("HOMETOWN")) return false;

        // expressao? → zero ou um, só tenta se tiver algo que inicia expressao
        if(temExpressao()){
            if(!expressao()) return false;
        }

        return matchL("}");
    }

    private boolean parametrosDeclaracao(){
        return tipo() && id() && parametrosDeclaracaoLinha();
    }

    private boolean parametrosDeclaracaoLinha(){
        if(peek() != null && peek().getTipo().equals(",")){
            return matchL(",") && tipo() && parametrosDeclaracao();
        }
        //epslon
        return true;
    }

    private boolean chamadaFuncao(){
        return id() && matchL("[") && parametrosChamada() && matchL("]") && matchL(";");
    }

    private boolean parametrosChamada(){
        return expressao() && parametrosChamadaLinha();
    }

    private boolean parametrosChamadaLinha(){
        if(peek() != null && peek().getTipo().equals(",")){
            return matchL(",") && expressao() && parametrosChamada();
        }
        //epslon
        return true;
    }

    private boolean ifs(){
        return matchL("houdini") && matchL("(") && condicao() && matchL(")")
                && matchL("{")
    }

    private boolean elses(){}

    private boolean whiles(){}

    private boolean doWhiles(){}

    private boolean fors(){}

    private boolean comando(){
        return comentario() && atribuicao() && entrada() && saida()
                && ifs() && whiles() && doWhiles() && fors() && chamadaFuncao();
    }

    private boolean main(){}

    private boolean codigo(){}

    private boolean matchT(String word){
        if(token.getTipo().equals(word)){
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean matchL(String word){
        if(token.getLexema().equals(word)){
            token = getNextToken();
            return true;
        }
        return false;
    }



}
