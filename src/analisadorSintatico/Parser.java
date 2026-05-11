package analisadorSintatico;

import analisadorLexico.Token;
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

    // Serve para casos onde há mais de uma definição da regra. Evita quebrar no meio da validação
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
        String t = peek().getTipo();
        // O léxico gera tipos EQ, GT, GE, LT, LE para os operadores de comparação
        // != e != não estão no MD, mas assumimos que o léxico os trata com tipos próprios
        return t.equals("EQ") || t.equals("GT") || t.equals("GE")
                || t.equals("LT") || t.equals("LE")
                || peek().getLexema().equals("!="); // confirmar com o léxico
    }

    public boolean isFormatador(){
        String l = peek().getLexema();
        return l.equals("%d") || l.equals("%f") || l.equals("%s");
    }

    private boolean temComando(){
        if(peek() == null) return false;
        String t = peek().getTipo();
        return t.equals("IF")
                || t.equals("WHILE")
                || t.equals("FOR")
                || t.equals("PRINT")
                || t.equals("INPUT")
                || t.equals("BREAK")
                || t.equals("INT")    // atribuicao com tipo
                || t.equals("FLOAT")  // atribuicao com tipo
                || t.equals("STRING") // atribuicao com tipo
                || t.equals("ID");    // atribuicao ou chamada de funcao
    }

    private boolean temExpressao(){
        if(peek() == null) return false;
        String t = peek().getTipo();
        return t.equals("ID")
                || t.equals("NUM_INT")
                || t.equals("NUM_FLOAT")
                || t.equals("STRING")
                || t.equals("AP");    // abre parentese "("
    }

    public void error(String regra){
        int linha = (token != null && token.getRef() != null) ? token.getRef().getLinha() : -1;
        System.out.println("Erro sintático na linha " + linha);
        System.out.println("=============");
        System.out.println("Regra: " + regra);
        System.out.println("Token inválido: " + token);
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
    string     → `"`palavra*`"`

    tipo       → `space` | `lithium` | `judas`
    atribuicao → tipo id `=` expressao `;`| id `=` expressao `;`
    declaracao → tipo id `;`

    comentario → `~~` palavra* `\n`

    expressao  → termo expressao'
    expressao' → op_arit termo expressao' | ε
    termo      → `(` expressao `)` | int | float | string | id | chamada_funcao

    condicao   → termo condicao'
    condicao'  → (op_logic | op_comp) termo condicao' | ε

    saida      → `catapult` `[` expressao `]` `;`

    entrada           → `pleaser`  `[` entrada' `]` `;`
    entrada'          → `"` ponteiros `"` `,` identificadores
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

    // ====== REGRAS DA GRAMÁTICA ======//

    private boolean id(){
        return matchT("ID"); // era "id", léxico gera "ID"
    }

    private boolean palavra(){
        // O léxico classifica sequências de letras como "ID"
        return matchT("ID");
    }

    private boolean op_arit(){
        // Operadores aritméticos chegam como lexemas, não têm tipo próprio no léxico
        return matchL("+") || matchL("-") || matchL("*")
                || matchL("/") || matchL("%");
    }

    private boolean op_comp(){
        // Léxico gera tipos EQ, GT, GE, LT, LE para comparadores
        return matchT("EQ") || matchT("GT") || matchT("GE")
                || matchT("LT") || matchT("LE")
                || matchL("!="); // confirmar com o léxico
    }

    private boolean op_logic(){
        return matchL("&&") || matchL("||");
    }

    private boolean tipo(){
        // Léxico mapeia: space→INT, lithium→FLOAT, judas→STRING
        return matchT("INT") || matchT("FLOAT") || matchT("STRING");
    }

    private boolean atribuicao(){
        if(peek() != null && (peek().getTipo().equals("INT")
                || peek().getTipo().equals("FLOAT")
                || peek().getTipo().equals("STRING"))){
            // tipo id = expressao ;
            return tipo() && id() && matchT("OP_ATRI") && expressao() && matchL(";");
        }
        // id = expressao ;
        return id() && matchT("OP_ATRI") && expressao() && matchL(";");
    }

    private boolean declaracao(){
        return tipo() && id() && matchL(";");
    }

    private boolean comentario(){
        // ~~ é reconhecido pelo léxico como lexema, NEW_LINE é o tipo de \n
        return matchL("~~") && palavra() && matchT("NEW_LINE");
    }

    private boolean expressao(){
        return termo() && expressaoLinha();
    }

    private boolean expressaoLinha(){
        if(peek() != null && isOpArit()){
            return op_arit() && termo() && expressaoLinha();
        }
        // ε
        return true;
    }

    private boolean termo(){
        if(peek() != null && peek().getTipo().equals("AP")) {
            // AP = abre parentese "("  |  FP = fecha parentese ")"
            return matchT("AP") && expressao() && matchT("FP");
        }
        return matchT("NUM_INT") || matchT("NUM_FLOAT") // era "int" e "float"
                || matchT("STRING") || matchT("ID");    // era "string" e "id"
    }

    private boolean condicao(){
        return termo() && condicaoLinha();
    }

    private boolean condicaoLinha(){
        if(peek() != null && (isOpLogic() || isOpComp())){
            return (op_logic() || op_comp()) && termo() && condicaoLinha(); // era op_arit()
        }
        // ε
        return true;
    }

    private boolean saida(){
        // catapult → tipo PRINT no léxico
        return matchT("PRINT") && matchL("[") && expressao() && matchL("]") && matchL(";");
    }

    private boolean entrada(){
        // pleaser → tipo INPUT no léxico
        return matchT("INPUT") && matchL("[") && entradaLinha() && matchL("]") && matchL(";");
    }

    private boolean entradaLinha(){
        return matchL("\"") && ponteiros() && matchL("\"") && matchL("COMMA") && identificadores();
    }

    private boolean ponteiros(){
        return formatadores() && ponteirosLinha();
    }

    private boolean ponteirosLinha(){
        if(peek() != null && peek().getLexema().equals(",")){
            return matchL("COMMA") && ponteiros();
        }
        // ε
        return true;
    }

    private boolean identificadores(){
        return id() && identificadoresLinha();
    }

    private boolean identificadoresLinha(){
        if(peek() != null && peek().getLexema().equals(",")){
            return matchL("COMMA") && identificadores();
        }
        // ε
        return true;
    }

    private boolean formatadores(){
        if(peek() != null && isFormatador()){
            return matchL("%d") || matchL("%f") || matchL("%s");
        }
        return false; // sem ε, formatador é obrigatório
    }

    private boolean declaracaoFuncao(){
        // PREY e HOMETOWN não estão no KeyWords, chegam como lexema com tipo ID
        // Confirmar com o léxico se PREY/HOMETOWN são keywords ou identificadores comuns
        if(!tipo() || !matchL("PREY") || !id() || !matchL("[")
                || !parametrosDeclaracao() || !matchL("]") || !matchT("AC"))
            return false;

        // comando* → zero ou mais
        while(temComando()){
            if(!comando()) return false;
        }

        if(!matchL("HOMETOWN")) return false;

        // expressao? → zero ou um
        if(temExpressao()){
            if(!expressao()) return false;
        }

        return matchT("FC"); // FC = fecha chave "}"
    }

    private boolean parametrosDeclaracao(){
        if(peek() != null && (peek().getTipo().equals("INT")
                || peek().getTipo().equals("FLOAT")
                || peek().getTipo().equals("STRING"))){
            return tipo() && id() && parametrosDeclaracaoLinha();
        }
        // ε — sem parâmetros
        return true;
    }

    private boolean parametrosDeclaracaoLinha(){
        if(peek() != null && peek().getLexema().equals(",")){
            return matchL("COMMA") && tipo() && id() && parametrosDeclaracaoLinha();
        }
        // ε
        return true;
    }

    private boolean chamadaFuncao(){
        return id() && matchL("[") && parametrosChamada() && matchL("]") && matchL(";");
    }

    private boolean parametrosChamada(){
        return expressao() && parametrosChamadaLinha();
    }

    private boolean parametrosChamadaLinha(){
        if(peek() != null && peek().getLexema().equals(",")){
            return matchL(",") && expressao() && parametrosChamadaLinha();
        }
        // ε
        return true;
    }

    private boolean ifs(){
        // houdini → tipo IF no léxico
        if(!matchT("IF") || !matchT("AP") || !condicao() || !matchT("FP") || !matchT("AC"))
            return false;

        while(temComando()){
            if(!comando()) return false;
        }

        return matchT("FC") && elses();
    }

    private boolean elses(){
        // more → tipo ELSE no léxico
        if(peek() != null && peek().getTipo().equals("ELSE")){
            if(!matchT("ELSE") || !matchT("AC")) return false;
            while(temComando()){
                if(!comando()) return false;
            }
            return matchT("FC");
        }
        // ε
        return true;
    }

    private boolean whiles(){
        // problems → tipo WHILE no léxico
        if(!matchT("WHILE") || !matchT("AP") || !condicao() || !matchT("FP") || !matchT("AC"))
            return false;

        while(temComando()){
            if(!comando()) return false;
        }

        return matchT("FC");
    }

    private boolean doWhiles(){
        // not...ok não está no KeyWords — chega como lexema, confirmar com o léxico
        if(!matchL("not...ok") || !matchT("AC")) return false;

        while(temComando()){
            if(!comando()) return false;
        }

        // "while" aqui é a palavra reservada problems? Confirmar com o amigo
        return matchT("FC") && matchT("WHILE")
                && matchT("AP") && condicao() && matchT("FP") && matchL(";");
    }

    private boolean fors(){
        // bloomfield → tipo FOR no léxico
        if(!matchT("FOR") || !matchT("AP")) return false;
        if(!atribuicao() || !condicao() || !atribuicao()) return false;
        if(!matchT("FP") || !matchT("AC")) return false;

        while(temComando()){
            if(!comando()) return false;
        }

        return matchT("FC");
    }

    private boolean comando(){
        if(peek() == null) return false;
        String t = peek().getTipo();
        String l = peek().getLexema();

        if(l.equals("~~"))    return comentario();
        if(t.equals("IF"))    return ifs();
        if(t.equals("WHILE")) return whiles();
        if(t.equals("FOR"))   return fors();
        if(t.equals("PRINT")) return saida();
        if(t.equals("INPUT")) return entrada();
        // ID pode ser atribuição (x = ...) ou chamada de função (f[...])
        // Precisaria de peek duplo pra distinguir, por ora trata como atribuição
        if(t.equals("ID"))    return atribuicao();
        if(t.equals("INT") || t.equals("FLOAT") || t.equals("STRING")) return atribuicao();
        return false;
    }

    public boolean main(){
        token = getNextToken(); // carrega o primeiro token antes de começar

        // declaracao_funcao*
        while(peek() != null && (peek().getTipo().equals("INT")
                || peek().getTipo().equals("FLOAT")
                || peek().getTipo().equals("STRING"))){
            if(!declaracaoFuncao()) return false;
        }

        // style → tipo START no léxico
        System.out.println("Antes do START: " + token);
        if(!matchT("START")) return false;

        System.out.println("Antes do CODIGO: " + token);
        codigo();

        // borderline → tipo END no léxico
        System.out.println("Antes do END: " + token);
        return matchT("END");
    }



    private boolean codigo(){
        // comando* codigo* | ε
        while(temComando()){
            if(!comando()) return false;
        }
        // ε
        return true;
    }

    // ====== MATCH ======//

    private boolean matchT(String word){
        if(token != null && token.getTipo().equals(word)){
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean matchL(String word){
        if(token != null && token.getLexema().equals(word)){
            token = getNextToken();
            return true;
        }
        return false;
    }
}