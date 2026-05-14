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

    public Token peek(){
        if(!tokens.isEmpty())
            return tokens.getFirst();
        return null;
    }

    // ====== HELPERS DE FIRST ======//

    private boolean isTipo(){
        if(token == null) return false;
        String t = token.getTipo();
        return t.equals("INT") || t.equals("FLOAT") || t.equals("STRING");
    }

    private boolean isOpArit(){
        if(token == null) return false;
        String l = token.getLexema();
        return l.equals("+") || l.equals("-") || l.equals("*")
                || l.equals("/") || l.equals("%");
    }

    private boolean isOpLogic(){
        if(token == null) return false;
        String l = token.getLexema();
        return l.equals("&&") || l.equals("||");
    }

    private boolean isOpComp(){
        if(token == null) return false;
        String t = token.getTipo();
        return t.equals("EQ") || t.equals("GT") || t.equals("GE")
                || t.equals("LT") || t.equals("LE")
                || token.getLexema().equals("!=");
    }

    private boolean isTermo(){
        if(token == null) return false;
        String t = token.getTipo();
        return t.equals("AP") || t.equals("NUM_INT") || t.equals("NUM_FLOAT")
                || t.equals("STR_LIT") || t.equals("ID");
    }

    private boolean temComando(){
        if(token == null) return false;
        String t = token.getTipo();
        String l = token.getLexema();
        return l.equals("~~")
                || t.equals("IF")
                || t.equals("WHILE")
                || t.equals("FOR")
                || t.equals("PRINT")
                || t.equals("INPUT")
                || t.equals("BREAK")
                || t.equals("INT")
                || t.equals("FLOAT")
                || t.equals("STRING")
                || t.equals("ID")
                || l.equals("not...ok");
    }

    private boolean temExpressao(){
        return isTermo();
    }

    // ====== ERRO E SUCESSO ======//

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

    // ====== REGRAS DA GRAMÁTICA ======//

    private boolean id(Node node){
        Node id = node.addNode("id");
        return matchT("ID", id);
    }

    private boolean palavra(Node node){
        Node palavra = node.addNode("palavra");
        return matchT("ID", palavra);
    }

    private boolean op_arit(Node node){
        Node op = node.addNode("op_arit");
        return matchL("+", op) || matchL("-", op) || matchL("*", op)
                || matchL("/", op) || matchL("%", op);
    }

    private boolean op_comp(Node node){
        Node op = node.addNode("op_comp");
        return matchT("EQ", op) || matchT("GT", op) || matchT("GE", op)
                || matchT("LT", op) || matchT("LE", op)
                || matchL("!=", op);
    }

    private boolean op_logic(Node node){
        Node op = node.addNode("op_logic");
        return matchL("&&", op) || matchL("||", op);
    }

    private boolean tipo(Node node){
        Node tipo = node.addNode("tipo");
        return matchT("INT", tipo) || matchT("FLOAT", tipo) || matchT("STRING", tipo);
    }

    /*
     * tipoPrefixo resolve o conflito de FIRST entre:
     *   declaracao_funcao → tipo PREY id [ ... ] { ... }
     *   atribuicao        → tipo id = expressao ;
     *   declaracao        → tipo id ;
     */
    private boolean tipoPrefixo(Node node){
        if(!tipo(node)) return false;

        if(token != null && token.getTipo().equals("FUNC_DECL")){
            return funcaoSufixo(node);
        }

        if(token != null && token.getTipo().equals("ID")){
            if(peek() != null && peek().getTipo().equals("OP_ATRI")){
                return id(node) && matchT("OP_ATRI", node) && expressao(node) && matchT("SEMI", node);
            }
            return id(node) && matchT("SEMI", node);
        }

        return false;
    }

    // sufixo da declaracao_funcao após consumir o tipo
    private boolean funcaoSufixo(Node node){
        Node funcao = node.addNode("declaracao_funcao");
        if(!matchT("FUNC_DECL", funcao) || !id(funcao) || !matchT("AB", funcao)
                || !parametrosDeclaracao(funcao) || !matchT("FB", funcao) || !matchT("AC", funcao))
            return false;

        while(temComando()){
            if(!comando(funcao)) return false;
        }

        if(!matchT("RETURN", funcao)) return false;

        if(temExpressao()){
            if(!expressao(funcao)) return false;
        }

        return matchT("FC", funcao);
    }

    private boolean comentario(Node node){
        Node com = node.addNode("comentario");
        return matchL("~~", com) && palavra(com) && matchT("NEW_LINE", com);
    }

    // expressao → termo expressao'
    private boolean expressao(Node node){
        Node expressao = node.addNode("expressao");
        return termo(expressao) && expressaoLinha(expressao);
    }

    // expressao' → op_arit termo expressao' | ε
    private boolean expressaoLinha(Node node){
        if(isOpArit()){
            Node expr = node.addNode("expressao'");
            return op_arit(expr) && termo(expr) && expressaoLinha(expr);
        }
        return true; // ε
    }

    // termo → ( expressao ) | int | float | string | id | chamada_funcao
    private boolean termo(Node node){
        if(token == null) return false;
        Node termo = node.addNode("termo");

        if(token.getTipo().equals("AP")){
            return matchT("AP", termo) && expressao(termo) && matchT("FP", termo);
        }

        if(token.getTipo().equals("ID")){
            if(peek() != null && peek().getTipo().equals("AB")){
                return chamadaFuncao(termo);
            }
            return id(termo);
        }

        return matchT("NUM_INT", termo) || matchT("NUM_FLOAT", termo) || matchT("STR_LIT", termo);
    }

    // condicao → termo condicao'
    private boolean condicao(Node node){
        Node condicao = node.addNode("condicao");
        return termo(condicao) && condicaoLinha(condicao);
    }

    // condicao' → (op_logic | op_comp) termo condicao' | ε
    private boolean condicaoLinha(Node node){
        if(isOpLogic() || isOpComp()){
            Node cond = node.addNode("condicao'");
            return (op_logic(cond) || op_comp(cond)) && termo(cond) && condicaoLinha(cond);
        }
        return true; // ε
    }

    // atribuicao → tipo id = expressao ; | id = expressao ;
    private boolean atribuicao(Node node){
        if(isTipo()){
            return tipoPrefixo(node);
        }
        Node atrib = node.addNode("atribuicao");
        return id(atrib) && matchT("OP_ATRI", atrib) && expressao(atrib) && matchT("SEMI", atrib);
    }

    // saida → PRINT ( expressao ) ;
    private boolean saida(Node node){
        Node saida = node.addNode("saida");
        return matchT("PRINT", saida) && matchT("AP", saida)
                && expressao(saida) && matchT("FP", saida) && matchT("SEMI", saida);
    }

    // entrada → INPUT ( entrada' ) ;
    private boolean entrada(Node node){
        Node entrada = node.addNode("entrada");
        return matchT("INPUT", entrada) && matchT("AP", entrada)
                && entradaLinha(entrada) && matchT("FP", entrada) && matchT("SEMI", entrada);
    }

    // entrada' → FORMAT_STRING , identificadores
    private boolean entradaLinha(Node node){
        Node entradaL = node.addNode("entrada'");
        return matchT("FORMAT_STRING", entradaL) && matchT("COMMA", entradaL)
                && identificadores(entradaL);
    }

    // identificadores → id identificadores'
    private boolean identificadores(Node node){
        Node ids = node.addNode("identificadores");
        return id(ids) && identificadoresLinha(ids);
    }

    // identificadores' → , identificadores | ε
    private boolean identificadoresLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node idsL = node.addNode("identificadores'");
            return matchT("COMMA", idsL) && identificadores(idsL);
        }
        return true; // ε
    }

    // parametros_declaracao → tipo id parametros_declaracao' | ε
    private boolean parametrosDeclaracao(Node node){
        if(isTipo()){
            Node params = node.addNode("parametros_declaracao");
            return tipo(params) && id(params) && parametrosDeclaracaoLinha(params);
        }
        return true; // ε
    }

    // parametros_declaracao' → , tipo id parametros_declaracao' | ε
    private boolean parametrosDeclaracaoLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node paramsL = node.addNode("parametros_declaracao'");
            return matchT("COMMA", paramsL) && tipo(paramsL) && id(paramsL)
                    && parametrosDeclaracaoLinha(paramsL);
        }
        return true; // ε
    }

    // chamada_funcao → id [ parametros_chamada ] ;
    private boolean chamadaFuncao(Node node){
        Node chamada = node.addNode("chamada_funcao");
        return id(chamada) && matchT("AB", chamada) && parametrosChamada(chamada)
                && matchT("FB", chamada) && matchT("SEMI", chamada);
    }

    // parametros_chamada → expressao parametros_chamada'
    private boolean parametrosChamada(Node node){
        Node params = node.addNode("parametros_chamada");
        return expressao(params) && parametrosChamadaLinha(params);
    }

    // parametros_chamada' → , expressao parametros_chamada' | ε
    private boolean parametrosChamadaLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node paramsL = node.addNode("parametros_chamada'");
            return matchT("COMMA", paramsL) && expressao(paramsL) && parametrosChamadaLinha(paramsL);
        }
        return true; // ε
    }

    // if → IF ( condicao ) { comando* } else
    private boolean ifs(Node node){
        Node ifs = node.addNode("if");
        if(!matchT("IF", ifs) || !matchT("AP", ifs) || !condicao(ifs)
                || !matchT("FP", ifs) || !matchT("AC", ifs))
            return false;

        while(temComando()){
            if(!comando(ifs)) return false;
        }

        return matchT("FC", ifs) && elses(ifs);
    }

    // else → ELSE { comando* } | ε
    private boolean elses(Node node){
        if(token != null && token.getTipo().equals("ELSE")){
            Node elses = node.addNode("else");
            if(!matchT("ELSE", elses) || !matchT("AC", elses)) return false;
            while(temComando()){
                if(!comando(elses)) return false;
            }
            return matchT("FC", elses);
        }
        return true; // ε
    }

    // while → WHILE ( condicao ) { comando* }
    private boolean whiles(Node node){
        Node whiles = node.addNode("while");
        if(!matchT("WHILE", whiles) || !matchT("AP", whiles) || !condicao(whiles)
                || !matchT("FP", whiles) || !matchT("AC", whiles))
            return false;

        while(temComando()){
            if(!comando(whiles)) return false;
        }

        return matchT("FC", whiles);
    }

    // do_while → not...ok { comando* } WHILE ( condicao ) ;
    private boolean doWhiles(Node node){
        Node doWhile = node.addNode("do_while");
        if(!matchL("not...ok", doWhile) || !matchT("AC", doWhile)) return false;

        while(temComando()){
            if(!comando(doWhile)) return false;
        }

        return matchT("FC", doWhile) && matchT("WHILE", doWhile)
                && matchT("AP", doWhile) && condicao(doWhile)
                && matchT("FP", doWhile) && matchT("SEMI", doWhile);
    }

    // for → FOR ( atribuicao condicao ; atribuicao ) { comando* }
    private boolean fors(Node node){
        Node fors = node.addNode("for");
        if(!matchT("FOR", fors) || !matchT("AP", fors)) return false;
        if(!atribuicao(fors)) return false;
        if(!condicao(fors) || !matchT("SEMI", fors)) return false;
        if(!atribuicao(fors)) return false;
        if(!matchT("FP", fors) || !matchT("AC", fors)) return false;

        while(temComando()){
            if(!comando(fors)) return false;
        }

        return matchT("FC", fors);
    }

    // comando → comentario | atribuicao | entrada | saida | if | while | do_while | for | chamada_funcao
    private boolean comando(Node node){
        if(token == null) return false;
        String t = token.getTipo();
        String l = token.getLexema();

        if(l.equals("~~"))       return comentario(node);
        if(t.equals("IF"))       return ifs(node);
        if(t.equals("WHILE"))    return whiles(node);
        if(t.equals("FOR"))      return fors(node);
        if(l.equals("not...ok")) return doWhiles(node);
        if(t.equals("PRINT"))    return saida(node);
        if(t.equals("INPUT"))    return entrada(node);
        if(isTipo())             return tipoPrefixo(node);

        if(t.equals("ID")){
            if(peek() != null && peek().getTipo().equals("AB")){
                return chamadaFuncao(node);
            }
            return atribuicao(node);
        }

        return false;
    }

    // main → declaracao_funcao* style codigo borderline
    public boolean main(){
        token = getNextToken();
        Node root = new Node("main");

        while(isTipo() && peek() != null && peek().getTipo().equals("FUNC_DECL")){
            if(!tipoPrefixo(root)) return false;
        }

        if(!matchT("START", root)) return false;
        if(!codigo(root)) return false;
        if(!matchT("END", root)) return false;

        // imprime a árvore ao final
        System.out.println(root.getTree());
        return true;
    }

    // codigo → comando* | ε
    private boolean codigo(Node node){
        Node codigo = node.addNode("codigo");
        while(temComando()){
            if(!comando(codigo)) return false;
        }
        return true; // ε
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

    private boolean matchT(String word, Node node){
        if(token != null && token.getTipo().equals(word)){
            node.addNode(token.getLexema());
            token = getNextToken();
            return true;
        }
        return false;
    }

    private boolean matchL(String word, Node node){
        if(token != null && token.getLexema().equals(word)){
            node.addNode(token.getLexema());
            token = getNextToken();
            return true;
        }
        return false;
    }
}