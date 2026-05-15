package analisadorSintatico;

import analisadorLexico.Token;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class Parser {

    List<Token> tokens;
    Token token;
    String traducao = "";   // acumula apenas o corpo (funções + main)
    boolean printArvore;
    boolean erroJaReportado = false;

    boolean usaIo = false;          // std::io — ativado por print ou input
    boolean usaInput = false;       // read_i32()
    boolean usaInputFloat = false;  // read_f64()
    boolean usaInputString = false; // read_string()

    public Parser(List<Token> tokens, boolean printArvore) {
        this.tokens = tokens;
        this.printArvore = printArvore;
    }

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.printArvore = false;
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

    private void traduz(String code){
        traducao += code;
    }

    /**
     * Monta o cabeçalho Rust de forma condicional, baseado nas flags
     * preenchidas durante o parse, e retorna o arquivo completo.
     */
    private String montarArquivoFinal(){
        StringBuilder sb = new StringBuilder();

        sb.append("#![allow(unused_mut)]\n\n");

        // std::io só é necessário quando há print ou input
        if(usaIo){
            sb.append("use std::io;\n\n");
        }

        // helpers de leitura — somente os tipos realmente usados
        if(usaInput){
            sb.append("fn read_i32() -> i32 {\n");
            sb.append("    let mut s = String::new();\n");
            sb.append("    io::stdin().read_line(&mut s).unwrap();\n");
            sb.append("    s.trim().parse().unwrap()\n");
            sb.append("}\n\n");
        }

        if(usaInputFloat){
            sb.append("fn read_f64() -> f64 {\n");
            sb.append("    let mut s = String::new();\n");
            sb.append("    io::stdin().read_line(&mut s).unwrap();\n");
            sb.append("    s.trim().parse().unwrap()\n");
            sb.append("}\n\n");
        }

        if(usaInputString){
            sb.append("fn read_string() -> String {\n");
            sb.append("    let mut s = String::new();\n");
            sb.append("    io::stdin().read_line(&mut s).unwrap();\n");
            sb.append("    s.trim().to_string()\n");
            sb.append("}\n\n");
        }

        sb.append(traducao);
        return sb.toString();
    }

    public void salvarArquivo(String caminho){
        try {
            FileWriter fw = new FileWriter(caminho);
            fw.write(montarArquivoFinal());
            fw.close();
            System.out.println("Arquivo Rust gerado em: " + caminho);
        } catch (IOException e) {
            System.out.println("Erro ao salvar arquivo: " + e.getMessage());
        }
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

    // ====== MAPEAMENTO DE TIPOS ======//

    // space → i32, lithium → f64, judas → String
    private String tipoParaRust(String lexema){
        switch(lexema){
            case "space":   return "i32";
            case "lithium": return "f64";
            case "judas":   return "String";
            default:        return lexema;
        }
    }

    // ====== ERRO E SUCESSO ======//

    public void error(String regra){
        if(erroJaReportado) return;
        erroJaReportado = true;
        int linha = (token != null) ? token.getLine() : -1;
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
        return matchT("ID", id); // sem traduz aqui
    }

    private boolean palavra(Node node){
        Node palavra = node.addNode("palavra");
        return matchT("ID", palavra);
    }

    private boolean op_arit(Node node){
        Node op = node.addNode("op_arit");
        if(token == null) return false;
        String l = token.getLexema();
        boolean ok = matchL("+", op) || matchL("-", op) || matchL("*", op)
                || matchL("/", op) || matchL("%", op);
        if(ok) traduz(" " + l + " ");
        return ok;
    }

    private boolean op_logic(Node node){
        Node op = node.addNode("op_logic");
        if(token == null) return false;
        String l = token.getLexema();
        boolean ok = matchL("&&", op) || matchL("||", op);
        if(ok) traduz(" " + l + " ");
        return ok;
    }

    private boolean op_comp(Node node){
        Node op = node.addNode("op_comp");
        if(token == null) return false;
        String l = token.getLexema();
        boolean ok = matchT("EQ", op) || matchT("GT", op) || matchT("GE", op)
                || matchT("LT", op) || matchT("LE", op)
                || matchL("!=", op);
        if(ok) traduz(" " + l + " ");
        return ok;
    }

    private boolean tipo(Node node){
        Node tipo = node.addNode("tipo");
        // tradução acontece no tipoPrefixo onde temos o contexto completo
        return matchT("INT", tipo) || matchT("FLOAT", tipo) || matchT("STRING", tipo);
    }

    /*
     * tipoPrefixo resolve o conflito de FIRST entre:
     *   declaracao_funcao → tipo PREY id [ ... ] { ... }
     *   atribuicao        → tipo id = expressao ;
     *   declaracao        → tipo id ;
     */
    private boolean tipoPrefixo(Node node){
        // salva o lexema do tipo antes de consumir
        String tipoLexema = token != null ? token.getLexema() : "";
        String tipoRust = tipoParaRust(tipoLexema);

        Node temp = new Node("temp");
        if(!tipo(temp)) return false;
        Node tipoNode = temp.nodes.get(0);

        if(token != null && token.getTipo().equals("FUNC_DECL")){
            Node funcao = node.addNode("declaracao_funcao");
            funcao.addNode(tipoNode);
            return funcaoSufixo(funcao, tipoRust);
        }

        if(token != null && token.getTipo().equals("ID")){
            String nomeId = token.getLexema();

            if(peek() != null && peek().getTipo().equals("OP_ATRI")){
                // atribuicao com tipo: let mut x: i32 = expressao;
                Node atrib = node.addNode("atribuicao");
                atrib.addNode(tipoNode);
                traduz("let mut " + nomeId + ": " + tipoRust + " = ");
                boolean ok = id(atrib) && matchT("OP_ATRI", atrib)
                        && expressao(atrib) && matchT("SEMI", atrib);
                if(ok) traduz(";\n");
                return ok;
            }

            // declaracao sem atribuição: let mut x: i32;
            Node decl = node.addNode("declaracao");
            decl.addNode(tipoNode);
            traduz("let mut " + nomeId + ": " + tipoRust + ";\n");
            return id(decl) && matchT("SEMI", decl);
        }

        return false;
    }

    // sufixo da declaracao_funcao após consumir o tipo
    // fn nome(params) -> tipo { ... }
    private boolean funcaoSufixo(Node node, String tipoRetornoRust){
        // PREY já foi identificado, salva o nome da função
        if(token == null || !token.getTipo().equals("FUNC_DECL")) return false;
        matchT("FUNC_DECL", node); // consome PREY

        String nomeFuncao = token != null ? token.getLexema() : "";

        // abre assinatura da função
        traduz("fn " + nomeFuncao);
        if(!id(node)) return false;

        traduz("(");
        if(!matchT("AB", node)) return false;
        if(!parametrosDeclaracao(node)) return false;
        traduz(")");
        if(!matchT("FB", node)) return false;

        traduz(" -> " + tipoRetornoRust + " {\n");
        if(!matchT("AC", node)) return false;

        while(temComando()){
            if(!comando(node)) return false;
        }

        // HOMETOWN = return
        if(!matchT("RETURN", node)) return false;
        traduz("    return ");

        if(temExpressao()){
            if(!expressao(node)) return false;
        }
        traduz(";\n");

        traduz("}\n\n");
        return matchT("FC", node);
    }

    private boolean comentario(Node node){
        Node com = node.addNode("comentario");
        traduz("// ");
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
            traduz("(");
            boolean ok = matchT("AP", termo) && expressao(termo) && matchT("FP", termo);
            traduz(")");
            return ok;
        }

        if(token.getTipo().equals("ID")){
            if(peek() != null && peek().getTipo().equals("AB")){
                return chamadaFuncao(termo);
            }
            traduz(token.getLexema()); // traduz aqui!
            return id(termo);
        }

        // NUM_INT, NUM_FLOAT, STR_LIT
        traduz(token.getLexema());
        if(matchT("NUM_INT", termo))   return true;
        if(matchT("NUM_FLOAT", termo)) return true;
        if(matchT("STR_LIT", termo))   return true;
        error("NUM_INT | NUM_FLOAT | STR_LIT");
        return false;
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
        if(isTipo()) return tipoPrefixo(node);
        Node atrib = node.addNode("atribuicao");
        String nomeId = token != null ? token.getLexema() : "";
        if(!id(atrib)) return false;
        traduz(nomeId + " = "); // depois de confirmar que é id
        boolean ok = matchT("OP_ATRI", atrib) && expressao(atrib) && matchT("SEMI", atrib);
        if(ok) traduz(";\n");
        return ok;
    }

    // saida → PRINT ( expressao ) ;
    // catapult(x)       → println!("{}", x)
    // catapult("texto") → println!("{}", "texto")
    private boolean saida(Node node){
        Node saida = node.addNode("saida");
        usaIo = true; // ativa std::io
        traduz("println!(\"{}\", ");
        if(!matchT("PRINT", saida) || !matchT("AB", saida)) return false;
        if(!expressao(saida)) return false;
        traduz(");\n");
        return matchT("FB", saida) && matchT("SEMI", saida);
    }

    // entrada → INPUT ( entrada' ) ;
    // pleaser("%d", x) → x = read_i32();
    private boolean entrada(Node node){
        Node entrada = node.addNode("entrada");
        usaIo = true; // ativa std::io
        return matchT("INPUT", entrada) && matchT("AB", entrada)
                && entradaLinha(entrada) && matchT("FB", entrada) && matchT("SEMI", entrada);
    }

    // entrada' → FORMAT_STRING , identificadores
    private boolean entradaLinha(Node node){
        Node entradaL = node.addNode("entrada'");

        // salva o FORMAT_STRING pra saber o tipo de leitura
        String formato = token != null ? token.getLexema() : "";
        if(!matchT("FORMAT_STRING", entradaL) || !matchT("COMMA", entradaL)) return false;

        // ativa a flag do helper correspondente e gera a chamada
        String funcLeitura = formatoParaRust(formato);
        traduz(token != null ? token.getLexema() : "");
        traduz(" = " + funcLeitura + ";\n");

        return identificadores(entradaL);
    }

    private String formatoParaRust(String formato){
        if(formato.contains("%d")){
            usaInput = true;        // precisa de read_i32()
            return "read_i32()";
        }
        if(formato.contains("%f")){
            usaInputFloat = true;   // precisa de read_f64()
            return "read_f64()";
        }
        if(formato.contains("%s")){
            usaInputString = true;  // precisa de read_string()
            return "read_string()";
        }
        // fallback: assume inteiro
        usaInput = true;
        return "read_i32()";
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
            String tipoLexema = token != null ? token.getLexema() : "";
            String tipoRust = tipoParaRust(tipoLexema);
            if(!tipo(params)) return false;
            String nomeParam = token != null ? token.getLexema() : "";
            traduz(nomeParam + ": " + tipoRust);
            return id(params) && parametrosDeclaracaoLinha(params);
        }
        return true; // ε
    }

    // parametros_declaracao' → , tipo id parametros_declaracao' | ε
    private boolean parametrosDeclaracaoLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node paramsL = node.addNode("parametros_declaracao'");
            traduz(", ");
            if(!matchT("COMMA", paramsL)) return false;
            String tipoLexema = token != null ? token.getLexema() : "";
            String tipoRust = tipoParaRust(tipoLexema);
            if(!tipo(paramsL)) return false;
            String nomeParam = token != null ? token.getLexema() : "";
            traduz(nomeParam + ": " + tipoRust);
            return id(paramsL) && parametrosDeclaracaoLinha(paramsL);
        }
        return true; // ε
    }

    // chamada_funcao → id [ parametros_chamada ] ;
    // minhaFuncao[x, y] → minhaFuncao(x, y);
    private boolean chamadaFuncao(Node node){
        Node chamada = node.addNode("chamada_funcao");
        String nomeFuncao = token != null ? token.getLexema() : "";
        traduz(nomeFuncao + "(");
        if(!id(chamada) || !matchT("AB", chamada)) return false;
        if(!parametrosChamada(chamada)) return false;
        traduz(");\n");
        return matchT("FB", chamada) && matchT("SEMI", chamada);
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
            traduz(", ");
            return matchT("COMMA", paramsL) && expressao(paramsL) && parametrosChamadaLinha(paramsL);
        }
        return true; // ε
    }

    // if → IF ( condicao ) { comando* } else
    // houdini(condicao) { } → if condicao { }
    private boolean ifs(Node node){
        Node ifs = node.addNode("if");
        traduz("if ");
        if(!matchT("IF", ifs) || !matchT("AP", ifs)) return false;
        if(!condicao(ifs)) return false;
        traduz(" {\n");
        if(!matchT("FP", ifs) || !matchT("AC", ifs)) return false;

        while(temComando()){
            if(!comando(ifs)) return false;
        }

        traduz("}\n");
        return matchT("FC", ifs) && elses(ifs);
    }

    // else → ELSE { comando* } | ε
    // more { } → else { }
    private boolean elses(Node node){
        if(token != null && token.getTipo().equals("ELSE")){
            Node elses = node.addNode("else");
            traduz("else {\n");
            if(!matchT("ELSE", elses) || !matchT("AC", elses)) return false;
            while(temComando()){
                if(!comando(elses)) return false;
            }
            traduz("}\n");
            return matchT("FC", elses);
        }
        return true; // ε
    }

    // while → WHILE ( condicao ) { comando* }
    // problems(condicao) { } → while condicao { }
    private boolean whiles(Node node){
        Node whiles = node.addNode("while");
        traduz("while ");
        if(!matchT("WHILE", whiles) || !matchT("AP", whiles)) return false;
        if(!condicao(whiles)) return false;
        traduz(" {\n");
        if(!matchT("FP", whiles) || !matchT("AC", whiles)) return false;

        while(temComando()){
            if(!comando(whiles)) return false;
        }

        traduz("}\n");
        return matchT("FC", whiles);
    }

    // do_while → not...ok { comando* } WHILE ( condicao ) ;
    // vira loop { ... if !(condicao) { break; } }
    private boolean doWhiles(Node node){
        Node doWhile = node.addNode("do_while");
        traduz("loop {\n");
        if(!matchL("not...ok", doWhile) || !matchT("AC", doWhile)) return false;

        while(temComando()){
            if(!comando(doWhile)) return false;
        }

        if(!matchT("FC", doWhile) || !matchT("WHILE", doWhile) || !matchT("AP", doWhile)) return false;
        traduz("if !(");
        if(!condicao(doWhile)) return false;
        traduz(") { break; }\n");
        traduz("}\n");
        return matchT("FP", doWhile) && matchT("SEMI", doWhile);
    }

    // for → FOR ( atribuicao condicao ; atribuicao ) { comando* }
    // bloomfield(init; cond; incr) { } → init while cond { ... incr }
    private boolean fors(Node node){
        Node fors = node.addNode("for");
        if(!matchT("FOR", fors) || !matchT("AP", fors)) return false;

        if(!atribuicao(fors)) return false;         // init

        traduz("while ");
        if(!condicao(fors) || !matchT("SEMI", fors)) return false; // condicao ;
        traduz(" {\n");

        if(!atribuicao(fors)) return false;         // incremento
        if(!matchT("FP", fors) || !matchT("AC", fors)) return false; // ) {

        while(temComando()){
            if(!comando(fors)) return false;
        }

        traduz("}\n");
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

        // O cabeçalho NÃO é emitido aqui; será montado em montarArquivoFinal()
        // após o parse, quando as flags já estiverem preenchidas.

        // declaracao_funcao* — antes do style
        while(isTipo() && peek() != null && peek().getTipo().equals("FUNC_DECL")){
            if(!tipoPrefixo(root)) return false;
        }

        // fn main() {
        traduz("fn main() {\n");
        if(!matchT("START", root)) return false;

        if(!codigo(root)) return false;

        traduz("}\n");
        if(!matchT("END", root)) return false;

        if(printArvore){
            System.out.println(root.getTree());
        }
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
        error(word); // token atual ainda é o inválido
        return false;
    }

    private boolean matchL(String word, Node node){
        if(token != null && token.getLexema().equals(word)){
            node.addNode(token.getLexema());
            token = getNextToken();
            return true;
        }
        error(word);
        return false;
    }
}