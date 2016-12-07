package compiler.element;

import compiler.MicroErrorMessages;
import compiler.MicroRuntimeException;
import compiler.SymbolMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
public abstract class Element {

    private static String FLOCAL_PREFIX = "$L";
    private static String RETURN = "$R";

    public enum Type {
        INT, FLOAT, STRING
    }

    public enum Context {
        VARIABLE, CONSTANT, TEMPORARY, LABEL, LINK, FLOCAL, FPARAM, RETURN
    }

    private Context ctx;
    private int ctxVal;
    private String name;
    private Type type;
    private String value;

    public Element() {}

    @Override
    public String toString() {
        String s = String.format("type %s", type);

        if (ctx != Context.VARIABLE)
            s += " context " + ctx;

        if (name != null)
            s += " name " + name;

        return s + " ref " + getRef();
        //return getRef();
    }

    public boolean isConstant() {
        return ctx == Context.CONSTANT;
    }

    public boolean isInt() {
        return type == Type.INT;
    }

    public boolean isFloat() {
        return type == Type.FLOAT;
    }

    public boolean isString() {
        return type == Type.STRING;
    }

    public boolean isTemporary() {
        return ctx == Context.TEMPORARY;
    }

    public abstract String getRef();

    public static Element getScopedElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, String id) {
        return scope.stream()
                .map(s -> symbolMaps.get(s).get(id))
                .filter(s -> s != null)
                .findFirst().orElse(null);
    }

}
