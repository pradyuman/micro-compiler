package compiler;

import compiler.element.Element;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.StringBuilder;
import java.util.LinkedHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public final class SymbolMap extends LinkedHashMap<String, Element> {

    private final String name;
    private final boolean function;
    private int numParam;

    public SymbolMap(String name) {
        super();
        this.name = name;
        this.function = false;
    }

    public SymbolMap(String name, boolean function) {
        super();
        this.name = name;
        this.function = function;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(String.format("Symbol table %s\n", name));
        entrySet().forEach(v -> b.append(String.format("%s\n", v.getValue().toString())));
        return b.toString();
    }

    @Override
    public Element put(String name, Element v) {
        if (containsKey(v.getName())) {
            throw new MicroRuntimeException(MicroErrorMessages.DuplicateDeclaration + ": " + v.getName());
        }
        return super.put(name, v);
    }

}
