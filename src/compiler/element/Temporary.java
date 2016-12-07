package compiler.element;

public class Temporary extends Element {

    private static String TEMP_PREFIX = "$T";

    public Temporary(int ctxVal) {
        super(Context.TEMPORARY, ctxVal, null, null, null);
    }

    public Temporary(int ctxVal, Type type) {
        super(Context.TEMPORARY, ctxVal, null, type, null);
    }

    @Override
    public String getRef() {
        return TEMP_PREFIX + getCtxVal();
    }
}
