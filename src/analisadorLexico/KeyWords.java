package analisadorLexico;

import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;

public class KeyWords{

    protected static final Map<String, String> keywords= new HashMap<>();

    static{
        //inicio de programa
        keywords.put("style", "START");
        keywords.put("borderline", "END");

        //tipo de variáveis
        keywords.put("space", "INT");
        keywords.put("lithium", "FLOAT");
        keywords.put("judas", "STRING");

        // scanf e printf
        keywords.put("pleaser", "INPUT");
        keywords.put("catapult", "PRINT");

        //condicionais
        keywords.put("houdini", "IF");
        keywords.put("more", "ELSE");

        //loops
        keywords.put("problems", "WHILE");
        keywords.put("bloomfield", "FOR");

        //parada
        keywords.put("loser", "BREAK");

    }

    public static boolean isKeyWord(String lexema){
        return keywords.containsKey(lexema);
    }

    public static String getTokenType(String lexema){
        return keywords.get(lexema);
    }

}
