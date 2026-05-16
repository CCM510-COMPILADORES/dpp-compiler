package analisadorSintatico;

import analisadorLexico.Token;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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

    private String montarArquivoFinal(){
        StringBuilder sb = new StringBuilder();

        sb.append("#![allow(unused_mut)]\n\n");

        if(usaIo){
            sb.append("use std::io;\n\n");
        }

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
        return expect("ID", id);
    }

    private boolean palavra(Node node){
        Node palavra = node.addNode("palavra");
        return expect("ID", palavra);
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
        // tentativas silenciosas — só reporta erro se nenhuma bater
        if(matchT("EQ", op))  { traduz(" " + l + " "); return true; }
        if(matchT("GT", op))  { traduz(" " + l + " "); return true; }
        if(matchT("GE", op))  { traduz(" " + l + " "); return true; }
        if(matchT("LT", op))  { traduz(" " + l + " "); return true; }
        if(matchT("LE", op))  { traduz(" " + l + " "); return true; }
        if(matchL("!=", op))  { traduz(" " + l + " "); return true; }
        error("op_comp — esperado: ==, !=, >, >=, <, <=");
        return false;
    }

    private boolean tipo(Node node){
        Node tipo = node.addNode("tipo");
        // tentativas silenciosas — só reporta erro se nenhuma bater
        if(matchT("INT", tipo))    return true;
        if(matchT("FLOAT", tipo))  return true;
        if(matchT("STRING", tipo)) return true;
        error("tipo — esperado: space, lithium ou judas");
        return false;
    }

    private boolean tipoPrefixo(Node node){
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
                Node atrib = node.addNode("atribuicao");
                atrib.addNode(tipoNode);
                traduz("let mut " + nomeId + ": " + tipoRust + " = ");
                boolean ok = id(atrib) && expect("OP_ATRI", atrib)
                        && expressao(atrib) && expect("SEMI", atrib);
                if(ok) traduz(";\n");
                return ok;
            }

            Node decl = node.addNode("declaracao");
            decl.addNode(tipoNode);
            traduz("let mut " + nomeId + ": " + tipoRust + ";\n");
            return id(decl) && expect("SEMI", decl);
        }

        return false;
    }

    private boolean funcaoSufixo(Node node, String tipoRetornoRust){
        if(token == null || !token.getTipo().equals("FUNC_DECL")) return false;
        matchT("FUNC_DECL", node);

        String nomeFuncao = token != null ? token.getLexema() : "";

        traduz("fn " + nomeFuncao);
        if(!id(node)) return false;

        traduz("(");
        if(!expect("AB", node)) return false;
        if(!parametrosDeclaracao(node)) return false;
        traduz(")");
        if(!expect("FB", node)) return false;

        traduz(" -> " + tipoRetornoRust + " {\n");
        if(!expect("AC", node)) return false;

        while(temComando()){
            if(!comando(node)) return false;
        }

        if(!expect("RETURN", node)) return false;
        traduz("    return ");

        if(temExpressao()){
            if(!expressao(node)) return false;
        }
        traduz(";\n");

        traduz("}\n\n");
        return expect("FC", node);
    }

    private boolean comentario(Node node){
        Node com = node.addNode("comentario");
        traduz("// ");
        return matchL("~~", com) && palavra(com) && expect("NEW_LINE", com);
    }

    private boolean expressao(Node node){
        Node expressao = node.addNode("expressao");
        return termo(expressao) && expressaoLinha(expressao);
    }

    private boolean expressaoLinha(Node node){
        if(isOpArit()){
            Node expr = node.addNode("expressao'");
            return op_arit(expr) && termo(expr) && expressaoLinha(expr);
        }
        return true;
    }

    // chamada como expressão — sem ; no final (usada dentro de atribuição)
    private boolean chamadaFuncaoExpr(Node node){
        Node chamada = node.addNode("chamada_funcao");
        String nomeFuncao = token != null ? token.getLexema() : "";
        traduz(nomeFuncao + "(");
        if(!id(chamada) || !expect("AB", chamada)) return false;
        if(!parametrosChamada(chamada)) return false;
        traduz(")");
        return expect("FB", chamada);
    }

    private boolean termo(Node node){
        if(token == null) return false;
        Node termo = node.addNode("termo");

        if(token.getTipo().equals("AP")){
            traduz("(");
            boolean ok = expect("AP", termo) && expressao(termo) && expect("FP", termo);
            traduz(")");
            return ok;
        }

        if(token.getTipo().equals("ID")){
            if(peek() != null && peek().getTipo().equals("AB")){
                return chamadaFuncaoExpr(termo); // versão sem ; no final
            }
            traduz(token.getLexema());
            return id(termo);
        }

        // tentativas silenciosas — só reporta erro se nenhuma bater
        traduz(token.getLexema());
        if(matchT("NUM_INT", termo))   return true;
        if(matchT("NUM_FLOAT", termo)) return true;
        if(matchT("STR_LIT", termo))   return true;
        error("termo — esperado: número, string ou identificador");
        return false;
    }

    private boolean condicao(Node node){
        Node condicao = node.addNode("condicao");
        return termo(condicao) && condicaoLinha(condicao);
    }

    private boolean condicaoLinha(Node node){
        if(isOpLogic() || isOpComp()){
            Node cond = node.addNode("condicao'");
            return (op_logic(cond) || op_comp(cond)) && termo(cond) && condicaoLinha(cond);
        }
        return true;
    }

    private boolean atribuicao(Node node){
        if(isTipo()) return tipoPrefixo(node);
        Node atrib = node.addNode("atribuicao");
        String nomeId = token != null ? token.getLexema() : "";
        if(!id(atrib)) return false;
        traduz(nomeId + " = ");
        boolean ok = expect("OP_ATRI", atrib) && expressao(atrib) && expect("SEMI", atrib);
        if(ok) traduz(";\n");
        return ok;
    }

    private boolean saida(Node node){
        Node saida = node.addNode("saida");
        usaIo = true;
        traduz("println!(\"{}\", ");
        if(!expect("PRINT", saida) || !expect("AB", saida)) return false;
        if(!expressao(saida)) return false;
        traduz(");\n");
        return expect("FB", saida) && expect("SEMI", saida);
    }

    private boolean entrada(Node node){
        Node entrada = node.addNode("entrada");
        usaIo = true;
        return expect("INPUT", entrada) && expect("AB", entrada)
                && entradaLinha(entrada) && expect("FB", entrada) && expect("SEMI", entrada);
    }

    private boolean entradaLinha(Node node){
        Node entradaL = node.addNode("entrada'");

        String formato = token != null ? token.getLexema() : "";
        if(!expect("FORMAT_STRING", entradaL) || !expect("COMMA", entradaL)) return false;

        String funcLeitura = formatoParaRust(formato);
        traduz(token != null ? token.getLexema() : "");
        traduz(" = " + funcLeitura + ";\n");

        return identificadores(entradaL);
    }

    private String formatoParaRust(String formato){
        if(formato.contains("%d")){ usaInput = true;       return "read_i32()";    }
        if(formato.contains("%f")){ usaInputFloat = true;  return "read_f64()";    }
        if(formato.contains("%s")){ usaInputString = true; return "read_string()"; }
        usaInput = true;
        return "read_i32()";
    }

    private boolean identificadores(Node node){
        Node ids = node.addNode("identificadores");
        return id(ids) && identificadoresLinha(ids);
    }

    private boolean identificadoresLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node idsL = node.addNode("identificadores'");
            return expect("COMMA", idsL) && identificadores(idsL);
        }
        return true;
    }

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
        return true;
    }

    private boolean parametrosDeclaracaoLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node paramsL = node.addNode("parametros_declaracao'");
            traduz(", ");
            if(!expect("COMMA", paramsL)) return false;
            String tipoLexema = token != null ? token.getLexema() : "";
            String tipoRust = tipoParaRust(tipoLexema);
            if(!tipo(paramsL)) return false;
            String nomeParam = token != null ? token.getLexema() : "";
            traduz(nomeParam + ": " + tipoRust);
            return id(paramsL) && parametrosDeclaracaoLinha(paramsL);
        }
        return true;
    }

    // chamada como comando standalone — com ; no final
    private boolean chamadaFuncao(Node node){
        Node chamada = node.addNode("chamada_funcao");
        String nomeFuncao = token != null ? token.getLexema() : "";
        traduz(nomeFuncao + "(");
        if(!id(chamada) || !expect("AB", chamada)) return false;
        if(!parametrosChamada(chamada)) return false;
        traduz(");\n");
        return expect("FB", chamada) && expect("SEMI", chamada);
    }

    private boolean parametrosChamada(Node node){
        Node params = node.addNode("parametros_chamada");
        return expressao(params) && parametrosChamadaLinha(params);
    }

    private boolean parametrosChamadaLinha(Node node){
        if(token != null && token.getTipo().equals("COMMA")){
            Node paramsL = node.addNode("parametros_chamada'");
            traduz(", ");
            return expect("COMMA", paramsL) && expressao(paramsL) && parametrosChamadaLinha(paramsL);
        }
        return true;
    }

    private boolean ifs(Node node){
        Node ifs = node.addNode("if");
        traduz("if ");
        if(!expect("IF", ifs) || !expect("AP", ifs)) return false;
        if(!condicao(ifs)) return false;
        traduz(" {\n");
        if(!expect("FP", ifs) || !expect("AC", ifs)) return false;

        while(temComando()){
            if(!comando(ifs)) return false;
        }

        traduz("}\n");
        return expect("FC", ifs) && elses(ifs);
    }

    private boolean elses(Node node){
        if(token != null && token.getTipo().equals("ELSE")){
            Node elses = node.addNode("else");
            traduz("else {\n");
            if(!expect("ELSE", elses) || !expect("AC", elses)) return false;
            while(temComando()){
                if(!comando(elses)) return false;
            }
            traduz("}\n");
            return expect("FC", elses);
        }
        return true;
    }

    private boolean whiles(Node node){
        Node whiles = node.addNode("while");
        traduz("while ");
        if(!expect("WHILE", whiles) || !expect("AP", whiles)) return false;
        if(!condicao(whiles)) return false;
        traduz(" {\n");
        if(!expect("FP", whiles) || !expect("AC", whiles)) return false;

        while(temComando()){
            if(!comando(whiles)) return false;
        }

        traduz("}\n");
        return expect("FC", whiles);
    }

    private boolean doWhiles(Node node){
        Node doWhile = node.addNode("do_while");
        traduz("loop {\n");
        if(!matchL("not...ok", doWhile) || !expect("AC", doWhile)) return false;

        while(temComando()){
            if(!comando(doWhile)) return false;
        }

        if(!expect("FC", doWhile) || !expect("WHILE", doWhile) || !expect("AP", doWhile)) return false;
        traduz("if !(");
        if(!condicao(doWhile)) return false;
        traduz(") { break; }\n");
        traduz("}\n");
        return expect("FP", doWhile) && expect("SEMI", doWhile);
    }

    private boolean fors(Node node){
        Node fors = node.addNode("for");
        if(!expect("FOR", fors) || !expect("AP", fors)) return false;

        if(!atribuicao(fors)) return false;

        traduz("while ");
        if(!condicao(fors) || !expect("SEMI", fors)) return false;
        traduz(" {\n");

        if(!atribuicao(fors)) return false;
        if(!expect("FP", fors) || !expect("AC", fors)) return false;

        while(temComando()){
            if(!comando(fors)) return false;
        }

        traduz("}\n");
        return expect("FC", fors);
    }

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

    public boolean main(){
        token = getNextToken();
        Node root = new Node("main");

        while(isTipo() && peek() != null && peek().getTipo().equals("FUNC_DECL")){
            if(!tipoPrefixo(root)) return false;
        }

        traduz("fn main() {\n");
        if(!expect("START", root)) return false;

        if(!codigo(root)) return false;

        traduz("}\n");
        if(!expect("END", root)) return false;

        if(printArvore){
            System.out.println(root.getTree());
        }
        return true;
    }

    private boolean codigo(Node node){
        Node codigo = node.addNode("codigo");
        while(temComando()){
            if(!comando(codigo)) return false;
        }
        return true;
    }

    // ====== MATCH ======//

    // silencioso — usado em tentativas com alternativas (tipo, op_comp, termo)
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

    // obrigatório — reporta erro se falhar
    private boolean expect(String word, Node node){
        if(token != null && token.getTipo().equals(word)){
            node.addNode(token.getLexema());
            token = getNextToken();
            return true;
        }
        error(word);
        return false;
    }

    private boolean expectL(String word, Node node){
        if(token != null && token.getLexema().equals(word)){
            node.addNode(token.getLexema());
            token = getNextToken();
            return true;
        }
        error(word);
        return false;
    }
}