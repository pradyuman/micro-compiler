package main;

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
        this.ctx = Context.CONSTANT;
        this.type = type;
        this.value = value;
    }

    // Variable with no value
    public Variable(String name, Type type) {
        this.ctx = Context.NORMAL;
        this.name = name;
        this.type = type;
    }

    // Variable with value
    public Variable(String name, Type type, String value) {
        this.ctx = Context.NORMAL;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    // RETURN
    public Variable(String name, Type type, Context ctx) {
        this.name = name;
        this.type = type;
        this.ctx = ctx;
    }

    // TEMP FLOCAL FPARAM
    public Variable(String name, Type type, Context ctx, int ctxVal) {
        this.name = name;
        this.type = type;
        this.ctx = ctx;
        this.ctxVal = ctxVal;
    }

    @Override
    public String toString() {
        String s = String.format("type %s", type);

        if (ctx != Context.NORMAL)
            s += " context " + ctx;

        if (name != null)
            s += " name " + name;

        return s + " stringref " + getStringRef();
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

    public boolean isTemp() {
        return ctx == Context.TEMP;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static Variable parseConstant(String id) {
        if (id.matches("[+-]?[0-9]+$"))
            return new Variable(Variable.Type.INT, id);

        if (id.matches("[+-]?([0-9]*[.])?[0-9]+"))
            return new Variable(Variable.Type.FLOAT, id);

        return null;
    }

    public String getStringRef() {
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
                throw new MicroException(MicroErrorMessages.UnknownVariableContext);
        }
    }
}
