package compiler.element;

public class Link extends Element {

    private int numLocal;
    private int numTemp;

    public Link(int numLocal, int numTemp) {
        super(Context.LINK, numLocal, null, null, null);
        this.numLocal = numLocal;
        this.numTemp = numTemp;
    }

    @Override
    public String getRef() {
        return String.valueOf(numLocal + numTemp);
    }

}
