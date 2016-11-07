import java.lang.StringBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

public class SymbolTable {

    public static class Variable {

        public static enum Type {
            INT, FLOAT, STRING
        }

        String name;
        Type type;
        String value;

        public Variable(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Variable(String name, Type type, String value) {
            this.name = name;
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            String s = "name " + name + " type " + type;
            if (type == Type.STRING) {
                s += " value " + value;
            }
            return s;
        }

    }

    private String name;
    private Map<String,Variable> variables;

    public SymbolTable(String name) {
        this.name = name;
        this.variables = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("Symbol table ").append(name).append("\n");
        for (Map.Entry<String,Variable> v: variables.entrySet()) {
            b.append(v.getValue().toString()).append("\n");
        }

        return b.toString();
    }

    public void put(String name, Variable v) {
        if (variables.containsKey(v.name)) {
            throw new MicroException("DECLARATION ERROR " + v.name);
        }

        variables.put(name, v);
    }

}
