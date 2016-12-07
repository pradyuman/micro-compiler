package compiler.expression;

import compiler.IR;
import compiler.SymbolMap;
import compiler.element.Element;
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
        return new Element(Element.Context.TEMP, register, null, null);
    }

    @Override
    public IR toIR(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, Expression.Node node, Integer register) {
        IR ir = new IR(symbolMaps.get(0));
        // if op1/op2 function, resolve function and add to ir
        // if not function, get element
        // if element is constant, do stuff

        Element op1 = node.get(0).getToken().toElement(symbolMaps, scope);
        Element op2 = node.get(1).getToken().toElement(symbolMaps, scope);
        Element.Type exprType = op1.isFloat() || op2.isFloat() ? Element.Type.FLOAT : Element.Type.INT;
        Element result = new Element(Element.Context.TEMP, register, null, exprType);
        ir.add(new IR.Node(IR.parseCalcOp(getValue(), exprType), op1, op2, result));
        return ir;
    }

}
