package compiler.expression;

import compiler.SymbolMap;
import compiler.element.Constant;
import compiler.element.Element;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
@AllArgsConstructor
public class Token {

    public enum Type {
        VAR, FUNCTION, FSEPARATOR, OPERATOR, LPAREN, RPAREN,
    }

    public static String calcop = "[-+*/(),]";

    private Type type;
    private String value;

    public boolean isFunction() {
        return type == Type.FUNCTION;
    }

    public boolean isLParen() {
        return type == Type.LPAREN;
    }

    public boolean isRParen() {
        return type == Type.RPAREN;
    }

    public boolean isOperator() {
        return type == Type.OPERATOR;
    }

    public boolean isVar() {
        return type == Type.VAR;
    }

    public Element toElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope) {
        Element el = Element.getScopedElement(symbolMaps, scope, value);
        if (el != null)
            return el;

        return Constant.parse(value);
    }

}
