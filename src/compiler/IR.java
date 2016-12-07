package compiler;

import compiler.element.Element;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Intermediate Representation
 */
@Setter
public class IR extends LinkedList<IR.Node> {

    public enum Opcode {
        ADDI, ADDF, SUBI, SUBF, MULTI, MULTF, DIVI, DIVF,
        STOREI, STOREF, GT, GE, LT, LE, NE, EQ,
        JUMP, LABEL, JSR, PUSH, POP, RETURN, LINK,
        READI, READF, WRITEI, WRITEF, WRITES
    }

    private static EnumSet<Opcode> ConditionalSet = EnumSet.of(
            Opcode.GT, Opcode.GE, Opcode.LT, Opcode.LE, Opcode.NE, Opcode.EQ
    );

    @Getter
    @Setter
    public static class Node {

        private Opcode opcode;
        private Element op1;
        private Element op2;
        private Element focus;
        private Set<Node> predecessors;
        private Set<Node> successors;
        private Set<Element> gen;
        private Set<Element> kill;
        private Set<Element> in;
        private Set<Element> out;

        // LINK RET JUMP-DEFER PUSH POP
        public Node(Opcode opcode) {
            this(opcode, null, null, null);
        }

        // JUMP LABEL READI READF WRITEI WRITEF JSR
        public Node(Opcode opcode, Element focus) {
            this(opcode, null, null, focus);
        }

        // STOREI STOREF
        public Node(Opcode opcode, Element op1, Element focus) {
            this(opcode, op1, null, focus);
        }

        public Node(Opcode opcode, Element op1, Element op2, Element focus) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.focus = focus;
            this.predecessors = new HashSet<>(2);
            this.successors = new HashSet<>(2);
            this.gen = new HashSet<>(2);
            this.kill = new HashSet<>(2);
            this.in = new LinkedHashSet<>(2);
            this.out = new LinkedHashSet<>(2);
        }

        @Override
        public String toString() {
            String s = opcode.toString();
            if (op1 != null) s += " " + op1.getRef();
            if (op2 != null) s += " " + op2.getRef();
            if (focus != null) s += " " + focus.getRef();
            return s;
        }

        public boolean isConditional() {
            return ConditionalSet.contains(opcode);
        }

        public boolean isJump() {
            return opcode == Opcode.JUMP;
        }

        public boolean isReturn() {
            return opcode == Opcode.RETURN;
        }

    }

    SymbolMap globalSymbolMap;
    int register;

    public IR(SymbolMap globalSymbolMap) {
        register = 0;
        this.globalSymbolMap = globalSymbolMap;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(";IR code\n");
        stream().map(n -> ";" + n + "\n").forEach(b::append);
        return b.toString();
    }

    public String cfgToString() {
        StringBuilder b = new StringBuilder();
        b.append("IR CFG\n");
        stream().map(n -> n + ":\nPredecessors:" + n.getPredecessors() + "\nSuccessors:" + n.getSuccessors() + "\n\n")
                .forEach(b::append);
        return b.toString();
    }

    public String setsToString() {
        StringBuilder b = new StringBuilder();
        b.append("IR GEN KILL IN OUT\n");
        stream().map(n -> {
            String s = n + ":\n";
            s += "PRE: " + n.getPredecessors() + "\n";
            s += "SUC: " + n.getSuccessors() + "\n";
            s += "GEN: " + n.getGen() + "\n";
            s += "KILL: " + n.getKill() + "\n";
            s += "IN: " + n.getIn() + "\n";
            s += "OUT: " + n.getOut() + "\n\n";
            return s;
        }).forEach(b::append);
        return b.toString();
    }

    public static Opcode parseCalcOp(String operator, Element.Type type) {
        switch (operator) {
            case "+":
                return type == Element.Type.INT ? Opcode.ADDI : Opcode.ADDF;
            case "-":
                return type == Element.Type.INT ? Opcode.SUBI : Opcode.SUBF;
            case "*":
                return type == Element.Type.INT ? Opcode.MULTI : Opcode.MULTF;
            case "/":
                return type == Element.Type.INT ? Opcode.DIVI : Opcode.DIVF;
            default:
                throw new MicroRuntimeException(MicroErrorMessages.UnknownCalcOp);
        }
    }

    public static Opcode parseCompOp(String operator, boolean opposite) {
        switch (operator) {
            case ">":
                return opposite ? Opcode.LE : Opcode.GT;
            case ">=":
                return opposite ? Opcode.LT : Opcode.GE;
            case "<":
                return opposite ? Opcode.GE : Opcode.LT;
            case "<=":
                return opposite ? Opcode.GT : Opcode.LE;
            case "!=":
                return opposite ? Opcode.EQ : Opcode.NE;
            case "=":
                return opposite ? Opcode.NE : Opcode.EQ;
            default:
                throw new MicroRuntimeException(MicroErrorMessages.UnknownCompOp);
        }
    }

    // Override to create gen and kill sets
    @Override
    public boolean add(IR.Node node) {
        generateGenAndKill(node);
        return super.add(node);
    }

    private void generateGenAndKill(IR.Node node) {
        boolean op1Valid = node.getOp1() != null && !node.getOp1().isConstant();
        boolean op2Valid = node.getOp2() != null && !node.getOp2().isConstant();
        boolean focusValid = node.getFocus() != null && !node.getFocus().isConstant();

        switch(node.getOpcode()) {
            case LABEL:
            case LINK:
                return;
            case PUSH:
            case WRITEI:
            case WRITEF:
            case WRITES:
                if (focusValid)
                    node.gen.add(node.getFocus());
                return;
            case POP:
            case READI:
            case READF:
                if (focusValid)
                    node.kill.add(node.getFocus());
                return;
            case JSR:
                globalSymbolMap.values().stream().forEach(node.gen::add);
                return;
            case GT:
            case GE:
            case LT:
            case LE:
            case NE:
            case EQ:
                if (op1Valid)
                    node.gen.add(node.getOp1());
                if (op2Valid)
                    node.gen.add(node.getOp2());
                return;
            default:
                if (op1Valid)
                    node.gen.add(node.getOp1());
                if (op2Valid)
                    node.gen.add(node.getOp2());
                if (focusValid)
                    node.kill.add(node.getFocus());
                return;
        }

    }

}
