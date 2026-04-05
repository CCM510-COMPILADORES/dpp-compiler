package analisadorLexico;

import java.text.CharacterIterator;

public class MathOperator extends AFD {

    public MathOperator(LexTable lexTable) {
        super(lexTable);
    }

    private Token createToken(String tipo, String lexema){
        return new Token(tipo, lexTable.add(lexema, 0));
    }

    @Override
    public Token evaluate(CharacterIterator code) {

        return switch (code.current()) {

            case '+' -> {
                code.next();
                yield createToken("PLUS", "+");
            }

            case '-' -> {
                code.next();
                yield createToken("SUB", "-");
            }

            case '*' -> {
                code.next();
                yield createToken("MULT", "*");
            }

            case '/' -> {
                code.next();
                yield createToken("DIV", "/");
            }

            case '(' -> {
                code.next();
                yield createToken("AP", "(");
            }

            case ')' -> {
                code.next();
                yield createToken("FP", ")");
            }

            case '{' -> {
                code.next();
                yield createToken("AC", "{");
            }

            case '}' -> {
                code.next();
                yield createToken("FC", "}");
            }

            case '\n' -> {
                code.next();
                yield createToken("NEW_LINE", "\\n");
            }

            case '=' -> {
                code.next();
                if (code.current() == '=') {
                    code.next();
                    yield createToken("EQ", "==");
                } else {
                    yield createToken("OP_ATRI", "=");
                }
            }

            case '>' -> {
                code.next();
                if (code.current() == '=') {
                    code.next();
                    yield createToken("GE", ">=");
                } else {
                    yield createToken("GT", ">");
                }
            }

            case '<' -> {
                code.next();
                if (code.current() == '=') {
                    code.next();
                    yield createToken("LE", "<=");
                } else {
                    yield createToken("LT", "<");
                }
            }

            case CharacterIterator.DONE -> {
                yield createToken("EOF", "$");
            }

            default -> null;
        };
    }
}