package analisadorLexico;

import java.util.HashMap;
import java.util.Map;

public class LexTable {

    private final Map<String, InputTable> table = new HashMap<>();

    public InputTable add(String lexema, int linha){
        if(!table.containsKey(lexema)){
            table.put(lexema, new InputTable(lexema, linha));
        }
        return table.get(lexema);
    }

    public void print(){
        table.values().forEach(System.out::println);
    }
}
