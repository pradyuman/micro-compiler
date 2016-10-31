import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

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
    private List<Variable> variables;

    public SymbolTable(String name) {
        this.name = name;
        this.variables = new ArrayList<Variable>();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        b.append("Symbol table ").append(name).append("\n");
        for (Variable v: variables) {
            b.append(v.toString()).append("\n");
        }

        return b.toString();
    }

    public void append(Variable v) {
        if (isInTable(v.name)) {
            throw new MicroException("DECLARATION ERROR " + v.name);
        }

        variables.add(v);
    }

    // Checks if a variable with name 's' exists in the table
    public boolean isInTable(String s) {
        return variables.stream().anyMatch((v) -> v.name.equals(s));
    }

}
