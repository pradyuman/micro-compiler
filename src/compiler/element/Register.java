package compiler.element;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Register extends Element {

    private static String PREFIX = "r";

    private int id;
    private Element data;
    private boolean dirty;

    public Register(int id, Element data, boolean dirty) {
        super(Context.REGISTER, id, null, null, null);
        this.id = id;
        this.data = data;
        this.dirty = dirty;
    }

    @Override
    public String toString() {
        return getRef() + " (" + data + ", dirty: " + dirty + ")";
    }

    @Override
    public String getRef() {
        return PREFIX + getId();
    }

}
