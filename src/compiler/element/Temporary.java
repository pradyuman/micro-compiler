package compiler.element;

public class Temporary extends Element {

    private static String PREFIX = "$T";

    public Temporary(int ctxVal) {
        super(Context.TEMPORARY, ctxVal, null, null, null);
    }

    public Temporary(int ctxVal, Type type) {
        super(Context.TEMPORARY, ctxVal, null, type, null);
    }

    @Override
    public String toString() {
        return getType() != null ? getRef() + " (" + getType() + ")" : getRef();
    }

    @Override
    public String getRef() {
        return PREFIX + getCtxVal();
    }

    @Override
    public Element getTinyElement(int localCount) {
        return new Stack(-(localCount + getCtxVal()), getType());
    }
}
