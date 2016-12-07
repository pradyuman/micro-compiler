package compiler.element;

public class FunctionParameter extends Element {

    private static String PREFIX = "$P";

    public FunctionParameter(int ctxVal, String name, Element.Type type) {
        super(Element.Context.FPARAM, ctxVal, name, type, null);
    }

    @Override
    public String getRef() {
        return PREFIX + getCtxVal();
    }
}
