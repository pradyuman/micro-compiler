package compiler.element;

public class Return extends Element {

    public Return(int numParam, Type type) {
        super(Context.RETURN, numParam, null, type, null);
    }

    @Override
    public String toString() {
        return getRef() + " (P: " + getCtxVal() + "}";
    }

    @Override
    public String getRef() {
        return "$R";
    }

    @Override
    public Element getTinyElement(int localCount) {
        return new Stack(5 + getCtxVal() + 1, getType());
    }
}
