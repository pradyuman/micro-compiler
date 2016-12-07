package compiler.element;

public class Return extends Element {

    public Return(int numParam, Type type) {
        super(Context.RETURN, numParam, null, type, null);
    }

    @Override
    public String getRef() {
        return "$R";
    }
}
