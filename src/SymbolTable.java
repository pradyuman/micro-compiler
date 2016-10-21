import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class SymbolTable {

    public static class Variable {
        String name;
        String type;
        String value;
    }

    private String name;
    private List<Variable> variables;

    public SymbolTable(String name) {
        this.name = name;
        this.variables = new ArrayList<Variable>();
    }

}
