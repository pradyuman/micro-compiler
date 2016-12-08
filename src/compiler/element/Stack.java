package compiler.element;

public class Stack extends Element {

    private static String PREFIX = "$";

    public Stack(int ctxVal, Type type) {
        super(Context.STACK, ctxVal, null, type, null);
    }

    @Override
    public String toString() {
        return getType() != null ? getRef() + " (" + getType() + ")" : getRef();
    }

    @Override
    public String getRef() {
        return PREFIX + getCtxVal();
    }
}
