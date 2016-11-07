public class Variable {

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

    public Variable(String name, Type type) {
        this.name = name;
        this.type = type;
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

    public String getValue() {
        return value;
    }
}
