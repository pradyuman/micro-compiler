package compiler.element;

public class Constant extends Element {

    Constant(Type type, String value) {
        super(Context.CONSTANT, 0, null, type, value);
    }

    public static Constant parse(String id) {
        if (id.matches("[+-]?[0-9]+$"))
            return new Constant(Element.Type.INT, id);

        if (id.matches("[+-]?([0-9]*[.])?[0-9]+"))
            return new Constant(Element.Type.FLOAT, id);

        return null;
    }

    @Override
    public String getRef() {
        return getValue();
    }

}
