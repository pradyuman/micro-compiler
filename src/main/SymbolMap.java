package main;

import java.lang.StringBuilder;
import java.util.LinkedHashMap;

public final class SymbolMap extends LinkedHashMap<String, Variable> {

    private String name;
    private boolean isFunction;
    private int numParam;

    public SymbolMap(String name) {
        super();
        this.name = name;
    }

    public SymbolMap(String name, boolean isFunction) {
        super();
        this.name = name;
        this.isFunction = isFunction;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Symbol table %s\n", name));
        entrySet().forEach(v -> b.append(String.format("%s\n", v.getValue().toString())));
        return b.toString();
    }

    @Override
    public Variable put(String name, Variable v) {
        if (containsKey(v.getName())) {
            throw new MicroException(MicroErrorMessages.DuplicateDeclaration + ": " + v.getName());
        }
        return super.put(name, v);
    }

    public boolean isFunction() {
        return isFunction;
    }

    public String getName() {
        return name;
    }

    public int getNumParam() {
        return numParam;
    }

    public void setNumParam(int num) {
        this.numParam = num;
    }
}
