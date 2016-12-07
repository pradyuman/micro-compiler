package compiler.expression;

import lombok.Getter;

@Getter
public class Function extends Token {

    private int numParam;

    public Function(String string, int numParam) {
        super(Type.FUNCTION, string);
        this.numParam = numParam;
    }

}
