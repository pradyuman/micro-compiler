package compiler.expression;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Operator extends Token {

    private int precedence;
    private int register;

    public Operator(String value) {
        super(Type.OPERATOR, value);
        if (value.matches("[<]")) this.precedence = 0;
        else this.precedence = value.matches("[+-]") ? 1 : 2;
    }

    public boolean isHigherPrecedence(Operator t) {
        return precedence > t.getPrecedence();
    }
}
