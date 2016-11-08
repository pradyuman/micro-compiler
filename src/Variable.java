public final class Variable {

    public enum Type {
        INT, FLOAT, STRING
    }

    private boolean constant;
    private String name;
    private Type type;
    private String value;

    public Variable(Type type, String value) {
        this.constant = true;
        this.type = type;
        this.value = value;
    }

    public Variable(String name, Type type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        String s = String.format("name %s type %s", name, type);
        if (type == Type.STRING) {
            s += String.format(" value %s", value);
        }
        return s;
    }

    public boolean isConstant() {
        return constant;
    }

    public boolean isInt() {
        return type == Type.INT;
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

    public static Variable generateConstant(String id) {
        if (id.matches("[+-]?[0-9]$"))
            return new Variable(Variable.Type.INT, id);

        if (id.matches("[+-]?([0-9]*[.])?[0-9]+"))
            return new Variable(Variable.Type.FLOAT, id);

        return null;
    }
}
