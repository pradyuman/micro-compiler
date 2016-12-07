package compiler.element;

public class Variable extends Element {

    public Variable(String name, Type type) {
        this(name, type, null);
    }

    public Variable(String name, Type type, String value) {
        super(Context.VARIABLE, 0, name, type, value);
    }

    @Override
    public String getRef() {
        return getName();
    }
}
