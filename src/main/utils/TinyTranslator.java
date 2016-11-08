package main.utils;

import main.IR;
import main.SymbolMap;
import main.Variable;

import java.util.*;

public class TinyTranslator {

    private int register;
    private Map<String, Integer> map;

    public TinyTranslator() {
        this.register = -1;
        this.map = new HashMap<>();
    }

    public void printTinyFromIR(List<SymbolMap> symbolMaps, List<IR.Node> ir) {
        System.out.println(";tiny code");
        Set<String> set = new HashSet<>();
        symbolMaps.forEach(m -> m.keySet().forEach(e -> set.add(e)));
        set.forEach(e -> System.out.format("var %s\n", e));
        ir.forEach(n -> {
            String op1 = resolveOp(n.getOp1());
            String op2 = resolveOp(n.getOp2());
            String focus = resolveFocus(n.getFocus());
            switch (n.getOpcode()) {
                case ADDI:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("addi %s r%s\n", op2, register);
                    break;
                case ADDF:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("addr %s r%s\n", op2, register);
                    break;
                case SUBI:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("subi %s r%s\n", op2, register);
                    break;
                case SUBF:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("subfr%s r%s\n", op2, register);
                    break;
                case MULTI:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("muli %s r%s\n", op2, register);
                    break;
                case MULTF:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("mulr %s r%s\n", op2, register);
                    break;
                case DIVI:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("divi %s r%s\n", op2, register);
                    break;
                case DIVF:
                    System.out.format("move %s r%s\n", op1, register);
                    System.out.format("divr %s r%s\n", op2, register);
                    break;
                case STOREI:
                case STOREF:
                   System.out.format("move %s %s\n", op1, focus);
                   break;
                case GT:
                    break;
                case GE:
                    break;
                case LT:
                    break;
                case LE:
                    break;
                case NE:
                    break;
                case EQ:
                    break;
                case JUMP:
                    break;
                case LABEL:
                    break;
                case READI:
                    System.out.format("sys readi %s\n", focus);
                    break;
                case READF:
                    System.out.format("sys readr %s\n", focus);
                    break;
                case WRITEI:
                    System.out.format("sys writei %s\n", focus);
                    break;
                case WRITEF:
                    System.out.format("sys writer %s\n", focus);
                    break;
            }
       });

        System.out.format("sys halt\n");
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
            map.put(focus.getName(), ++register);
            return "r" + register;
        }
        return focus.getName();
    }
}
