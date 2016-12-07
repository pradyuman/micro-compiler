package compiler.expression;

import compiler.SymbolMap;
import compiler.element.Element;
import compiler.element.Temporary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.LinkedList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Operator extends Token {

    private int precedence;
    private int register;

    public Operator(String value) {
        super(Type.OPERATOR, value);
        if (value.matches("[<]"))
            this.precedence = 0;
        else
            this.precedence = value.matches("[+-]") ? 1 : 2;
    }

    public boolean isHigherPrecedence(Operator t) {
        return precedence > t.getPrecedence();
    }

    @Override
    public Element toElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope) {
        return new Temporary(register);
    }

}
