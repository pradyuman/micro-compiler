package main.utils;

import main.IR;
import main.SymbolMap;
import main.Variable;

import java.util.*;

public class TinyTranslator {

    private static final Map<IR.Opcode, String> dict;

    static {
        Map<IR.Opcode, String> _dict = new HashMap<>();
        _dict.put(IR.Opcode.ADDI, "addi");
        _dict.put(IR.Opcode.ADDF, "addr");
        _dict.put(IR.Opcode.SUBI, "subi");
        _dict.put(IR.Opcode.SUBF, "subr");
        _dict.put(IR.Opcode.MULTI, "muli");
        _dict.put(IR.Opcode.MULTF, "mulr");
        _dict.put(IR.Opcode.DIVI, "divi");
        _dict.put(IR.Opcode.DIVF, "divr");
        _dict.put(IR.Opcode.GT, "jgt");
        _dict.put(IR.Opcode.GE, "jge");
        _dict.put(IR.Opcode.LT, "jlt");
        _dict.put(IR.Opcode.LE, "jle");
        _dict.put(IR.Opcode.NE, "jne");
        _dict.put(IR.Opcode.EQ, "jeq");
        _dict.put(IR.Opcode.JUMP, "jmp");
        _dict.put(IR.Opcode.LABEL, "label");
        _dict.put(IR.Opcode.READI, "sys readi");
        _dict.put(IR.Opcode.READF, "sys readr");
        _dict.put(IR.Opcode.WRITEI, "sys writei");
        _dict.put(IR.Opcode.WRITEF, "sys writer");

        dict = Collections.unmodifiableMap(_dict);
    }

    private int register;
    private Map<String, Integer> map;

    public TinyTranslator() {
        this.register = -1;
        this.map = new HashMap<>();
    }

    public void printTinyFromIR(List<SymbolMap> symbolMaps, List<IR.Node> ir) {
        System.out.println(";tiny code");
        symbolMaps.stream().flatMap(m -> m.keySet().stream()).distinct()
                .forEach(e -> System.out.format("var %s\n", e));
        ir.forEach(n -> {
            String op1 = resolveOp(n.getOp1());
            String op2 = resolveOp(n.getOp2());
            String focus = resolveFocus(n.getFocus());
            String command = dict.get(n.getOpcode());

            switch(n.getType()) {
                case CALC:
                    System.out.format("move %s %s\n", op1, focus);
                    System.out.format("%s %s %s\n", command, op2, focus);
                    break;
                case COMP:
                    String comp = resolveComp(n.getOp1(), n.getOp2());
                    if (!n.getOp2().isTemp()) {
                        System.out.format("move %s r%s\n", op2, ++register);
                        op2 = "r" + register;
                    }
                    System.out.format("%s %s %s\n", comp, op1, op2);
                    System.out.format("%s %s\n", command, focus);
                    break;
                case STORE:
                    if (!n.getOp1().isTemp() && !n.getFocus().isTemp()) {
                        System.out.format("move %s r%s\n", op1, ++register);
                        op1 = "r" + register;
                    }
                    System.out.format("move %s %s\n", op1, focus);
                    break;
                default:
                    System.out.format("%s %s\n", command, focus);
            }
       });

        System.out.println("sys halt");
    }

    private String resolveComp(Variable op1, Variable op2) {
        if ((op1 != null && op1.isFloat()) || (op2 != null && op2.isFloat()))
            return "cmpr";
        else
            return "cmpi";
    }

    private String resolveOp(Variable op) {
        if (op == null)
            return null;

        if (op.isTemp())
            return "r" + map.get(op.getName());

        if (op.isConstant())
            return op.getValue();

        return op.getName();
    }

    private String resolveFocus(Variable focus) {
        if (focus.isTemp()) {
            map.put(focus.getRef(), ++register);
            return "r" + register;
        }
        return focus.getName();
    }
}
