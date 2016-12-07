package compiler.expression;

import compiler.IR;
import compiler.SymbolMap;
import compiler.element.Element;
import lombok.Data;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Function extends Token {

    private int numParam;

    public Function(String string, int numParam) {
        super(Type.FUNCTION, string);
        this.numParam = numParam;
    }

    @Override
    public Element toElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope) {
        IR ir = toIR(symbolMaps, scope);
        return
    }

    @Override
    public IR toIR(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, Expression.Node node, Integer register) {
        IR ir = new IR(symbolMaps.get(0));
        ir.add(new IR.Node(IR.Opcode.PUSH));
        node.forEach(param -> ir.add(new IR.Node(IR.Opcode.PUSH, param.getToken().toElement())));
        ir.add(new IR.Node(IR.Opcode.JSR,
                new Element(node.getToken().getValue(), Element.Type.STRING)));
        node.forEach(p -> ir.add(new IR.Node(IR.Opcode.POP)));
        ir.add(new IR.Node(IR.Opcode.POP,
                new Element(Element.Context.TEMP, register++, null, null)));
        return ir;
    }
}
