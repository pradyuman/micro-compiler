import java.lang.StringBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SymbolMap extends LinkedHashMap<String, Variable> {

    private String name;

    public SymbolMap(String name) {
        super();
        this.name = name;
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
            throw new MicroException("DECLARATION ERROR " + v.getName());
        }
        return super.put(name, v);
    }

}
