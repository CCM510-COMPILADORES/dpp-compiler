import analisadorLexico.Lexer;
import analisadorLexico.Token;
import analisadorSintatico.Parser;

import java.util.List;

public class ParserTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // rode todos os grupos de teste
        testarAtribuicao();
        testarSaida();
        testarEntrada();
        testarIf();
        testarWhile();
        testarFor();
        testarFuncao();
        testarAninhamento();

        // resultado final
        System.out.println("\n==============================");
        System.out.println("Passou : " + passed);
        System.out.println("Falhou : " + failed);
        System.out.println("==============================");
    }

    // ====== ENGINE ======//

    private static void test(String descricao, String codigo, boolean esperado) {
        try {
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.getTokens();
            Parser parser = new Parser(tokens);
            boolean resultado = parser.main();

            if (resultado == esperado) {
                System.out.println("✅ " + descricao);
                passed++;
            } else {
                System.out.println("❌ " + descricao + " → esperado: " + esperado + ", obtido: " + resultado);
                failed++;
            }
        } catch (Exception e) {
            System.out.println("💥 " + descricao + " → exceção: " + e.getMessage());
            failed++;
        }
    }

    // ====== TESTES POR REGRA ======//

    private static void testarAtribuicao() {
        System.out.println("\n--- Atribuição ---");
        test("atribuição com tipo int",        "style space x = 10; borderline",         true);
        test("atribuição com tipo float",      "style lithium x = 1.5; borderline",      true);
        test("atribuição sem tipo",            "style x = 10; borderline",               true);
        test("atribuição sem ;",               "style space x = 10 borderline",          false);
        test("atribuição sem valor",           "style space x = ; borderline",           false);
    }

    private static void testarSaida() {
        System.out.println("\n--- Saída (catapult) ---");
        test("saída com id",                   "style catapult(x); borderline",          true);
        test("saída com número",               "style catapult(42); borderline",         true);
        test("saída com string",               "style catapult(\"oi\"); borderline",     true);
        test("saída com expressão",            "style catapult(x + 1); borderline",      true);
        test("saída sem ;",                    "style catapult(x) borderline",           false);
        test("saída sem )",                    "style catapult(x; borderline",           false);
    }

    private static void testarIf() {
        System.out.println("\n--- If/Else ---");
        test("if simples",                     "style houdini(x == 10) { catapult(x); } borderline",              true);
        test("if com else",                    "style houdini(x == 10) { catapult(x); } more { catapult(y); } borderline", true);
        test("if sem {",                       "style houdini(x == 10) catapult(x); borderline",                  false);
        test("if sem condição",                "style houdini() { catapult(x); } borderline",                     false);
    }

    private static void testarEntrada() {
        System.out.println("\n--- Entrada (pleaser) ---");
        test("entrada simples",                 "style pleaser(\"%d\", x); borderline",                  true);
        test("entrada com float",               "style pleaser(\"%f\", x); borderline",                  true);
        test("entrada com string",              "style pleaser(\"%s\", x); borderline",                  true);
        test("entrada múltiplos formatadores",  "style pleaser(\"%d, %f\", x, y); borderline",           true);
        test("entrada múltiplos ids",           "style pleaser(\"%d, %d\", x, y); borderline",           true);
        test("entrada sem ;",                   "style pleaser(\"%d\", x) borderline",                   false);
        test("entrada sem formatador",          "style pleaser(\"\", x); borderline",                    false);
        test("entrada sem id",                  "style pleaser(\"%d\"); borderline",                     false);
        test("entrada sem )",                   "style pleaser(\"%d\", x; borderline",                   false);
    }

    private static void testarWhile() {
        System.out.println("\n--- While (problems) ---");
        test("while simples",                   "style problems(x < 10) { catapult(x); } borderline",                      true);
        test("while com atribuição dentro",     "style problems(x < 10) { x = x + 1; } borderline",                       true);
        test("while aninhado",                  "style problems(x < 10) { problems(y < 5) { catapult(y); } } borderline",  true);
        test("while sem condição",              "style problems() { catapult(x); } borderline",                            false);
        test("while sem {",                     "style problems(x < 10) catapult(x); borderline",                          false);
        test("while sem }",                     "style problems(x < 10) { catapult(x); borderline",                        false);
        test("while sem ;  no comando interno", "style problems(x < 10) { catapult(x) } borderline",                       false);
    }

    private static void testarFor() {
        System.out.println("\n--- For (bloomfield) ---");
        test("for simples",          "style bloomfield(space x = 0; x < 10; x = x + 1;) { catapult(x); } borderline",  true);
        test("for sem tipo na init", "style bloomfield(x = 0; x < 10; x = x + 1;) { catapult(x); } borderline",        true);
        test("for corpo vazio",      "style bloomfield(space x = 0; x < 10; x = x + 1;) { } borderline",               true);
        test("for sem ultimo ;",     "style bloomfield(space x = 0; x < 10; x = x + 1) { catapult(x); } borderline",   false); // inválido!
        test("for sem condição",     "style bloomfield(space x = 0; ; x = x + 1;) { catapult(x); } borderline",        false);
        test("for sem {",            "style bloomfield(space x = 0; x < 10; x = x + 1;) catapult(x); borderline",      false);
    }

    private static void testarFuncao() {
        System.out.println("\n--- Declaração de Função (PREY) ---");
        test("função sem parâmetros",      "space PREY minhaFuncao[] { HOMETOWN } style borderline",                               true);
        test("função com um parâmetro",    "space PREY minhaFuncao[space x] { HOMETOWN } style borderline",                        true);
        test("função com dois parâmetros", "space PREY minhaFuncao[space x, lithium y] { HOMETOWN } style borderline",             true);
        test("função com retorno",         "space PREY minhaFuncao[space x] { HOMETOWN x } style borderline",                      true);
        test("função com corpo vazio",     "space PREY minhaFuncao[] { HOMETOWN } style borderline",                               true);
        test("função sem HOMETOWN",        "space PREY minhaFuncao[] { catapult(x); } style borderline",                           false);
        test("função sem {",               "space PREY minhaFuncao[] catapult(x); HOMETOWN style borderline",                      false);
        test("função sem ]",               "space PREY minhaFuncao[ catapult(x); HOMETOWN style borderline",                       false);
    }

    private static void testarAninhamento() {
        System.out.println("\n--- Aninhamento ---");

        // if dentro de if
        test("if dentro de if",
                "style houdini(x == 10) { houdini(y == 5) { catapult(x); } } borderline",
                true);

        // if dentro de if com else
        test("if dentro de if com else",
                "style houdini(x == 10) { houdini(y == 5) { catapult(x); } more { catapult(y); } } borderline",
                true);

        // while dentro de while
        test("while dentro de while",
                "style problems(x < 10) { problems(y < 5) { catapult(y); } } borderline",
                true);

        // for dentro de for
        test("for dentro de for",
                "style bloomfield(space x = 0; x < 10; x = x + 1;) { bloomfield(space y = 0; y < 5; y = y + 1;) { catapult(y); } } borderline",
                true);

        // while dentro de if
        test("while dentro de if",
                "style houdini(x == 10) { problems(y < 5) { catapult(y); } } borderline",
                true);

        // if dentro de while
        test("if dentro de while",
                "style problems(x < 10) { houdini(x == 5) { catapult(x); } } borderline",
                true);

        // for dentro de while
        test("for dentro de while",
                "style problems(x < 10) { bloomfield(space y = 0; y < 5; y = y + 1;) { catapult(y); } } borderline",
                true);

        // três níveis de aninhamento
        test("três níveis de aninhamento",
                "style houdini(x == 10) { problems(y < 5) { houdini(y == 3) { catapult(y); } } } borderline",
                true);

        // aninhamento inválido — falta } interno
        test("aninhamento sem } interno",
                "style houdini(x == 10) { problems(y < 5) { catapult(y); } borderline",
                false);

        // aninhamento inválido — falta condição no while interno
        test("while interno sem condição",
                "style houdini(x == 10) { problems() { catapult(y); } } borderline",
                false);
    }
}