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

    public enum Type {
        INT, FLOAT, STRING
    }

    public enum Context {
        VARIABLE, CONSTANT, TEMPORARY, REGISTER, STACK,
        LABEL, LINK, FLOCAL, FPARAM, RETURN
    }

    private Context ctx;
    private int ctxVal;
    private String name;
    private Type type;
    private String value;

    public Element() {}

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

    public boolean isReturn() {
        return ctx == Context.RETURN;
    }

    public abstract String getRef();

    public Element getTinyElement(int localCount) {
        return this;
    }

    public static Element getScopedElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, String id) {
        return scope.stream()
                .map(s -> symbolMaps.get(s).get(id))
                .filter(s -> s != null)
                .findFirst().orElse(null);
    }

}
