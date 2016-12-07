package compiler.element;

public class FunctionLocal extends Element{

    private static String PREFIX = "$L";

    public FunctionLocal(int ctxVal, String name, Element.Type type) {
        super(Element.Context.FLOCAL, ctxVal, name, type, null);
    }

    @Override
    public String toString() {
        return getRef() + " (" + getType() + " " + getName() + ")";
    }

    @Override
    public String getRef() {
        return PREFIX + getCtxVal();
    }
}
