package compiler.element;

public class Label extends Element {

    public Label(String name) {
        super(Element.Context.LABEL, 0, name, null, null);
    }

    @Override
    public String getRef() {
        return getName();
    }

}
