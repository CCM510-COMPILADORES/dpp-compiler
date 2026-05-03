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

    /*private boolean comentario(){
        return matchL("~~") && palavra() && matchL("\n");
    }*/

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
