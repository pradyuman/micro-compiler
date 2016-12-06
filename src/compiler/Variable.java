package compiler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Variable {

    private static String FLOCAL_PREFIX = "$L";
    private static String FPARAM_PREFIX = "$P";
    private static String TEMP_PREFIX = "$T";
    private static String RETURN = "$R";

    public enum Type {
        INT, FLOAT, STRING
    }

    public enum Context {
        NORMAL, CONSTANT, TEMP, FLOCAL, FPARAM, RETURN
    }

    private Context ctx;
    private int ctxVal;
    private String name;
    private Type type;
    private String value;

    // Constant
    public Variable(Type type, String value) {
        this(Context.CONSTANT, 0, null, type, value);
    }

    // Variable with value
    public Variable(String name, Type type, String value) {
        this(Context.NORMAL, 0, name, type, value);
    }

    // Variable with no value
    public Variable(String name, Type type) {
        this(name, type, null);
    }

    // Link has ctxVal
    public Variable(int ctxVal, String name, Type type) {
        this(Context.NORMAL, ctxVal, name, type);
    }

    // TEMP FLOCAL FPARAM RETURN
    public Variable(Context ctx, int ctxVal, String name, Type type) {
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

    public boolean isTemp() {
        return ctx == Context.TEMP;
    }

    public static Variable parseConstant(String id) {
        if (id.matches("[+-]?[0-9]+$"))
            return new Variable(Variable.Type.INT, id);

        if (id.matches("[+-]?([0-9]*[.])?[0-9]+"))
            return new Variable(Variable.Type.FLOAT, id);

        return null;
    }

    public String getRef() {
        switch (ctx) {
            case NORMAL:
                return name;
            case CONSTANT:
                return value;
            case TEMP:
                return TEMP_PREFIX + ctxVal;
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
}
