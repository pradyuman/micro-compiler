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
public class Element {

    private static String FLOCAL_PREFIX = "$L";
    private static String FPARAM_PREFIX = "$P";
    private static String RETURN = "$R";

    public enum Type {
        INT, FLOAT, STRING
    }

    public enum Context {
        NORMAL, CONSTANT, TEMPORARY, FLOCAL, FPARAM, RETURN
    }

    private Context ctx;
    private int ctxVal;
    private String name;
    private Type type;
    private String value;

    public Element() {}

    // Element with value
    public Element(String name, Type type, String value) {
        this(Context.NORMAL, 0, name, type, value);
    }

    // Element with no value
    public Element(String name, Type type) {
        this(name, type, null);
    }

    // Link has ctxVal
    public Element(int ctxVal, String name, Type type) {
        this(Context.NORMAL, ctxVal, name, type);
    }

    // TEMP FLOCAL FPARAM RETURN
    public Element(Context ctx, int ctxVal, String name, Type type) {
        this(ctx, ctxVal, name, type, null);
    }

    @Override
    public String toString() {
        String s = String.format("type %s", type);

        if (ctx != Context.NORMAL)
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

    public String getRef() {
        switch (ctx) {
            case NORMAL:
                return name;
            case CONSTANT:
                return value;
            case FLOCAL:
                return FLOCAL_PREFIX + ctxVal;
            case FPARAM:
                return FPARAM_PREFIX + ctxVal;
            case RETURN:
                return RETURN;
            default:
                throw new MicroRuntimeException(MicroErrorMessages.UnknownVariableContext);
        }
    }

    public static Element getScopedElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, String id) {
        return scope.stream()
                .map(s -> symbolMaps.get(s).get(id))
                .filter(s -> s != null)
                .findFirst().orElse(null);
    }

}
