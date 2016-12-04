package main;

import lombok.Data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Intermediate Representation
 */
public class IR extends LinkedList<IR.Node> {

    public enum Opcode {
        ADDI, ADDF, SUBI, SUBF, MULTI, MULTF, DIVI, DIVF,
        STOREI, STOREF, GT, GE, LT, LE, NE, EQ,
        JUMP, LABEL, JSR, PUSH, POP, RET, LINK,
        READI, READF, WRITEI, WRITEF, WRITES
    }

    private static EnumSet<Opcode> Conditional = EnumSet.of(
            Opcode.GT, Opcode.GE, Opcode.LT, Opcode.LE, Opcode.NE, Opcode.EQ
    );

    @Data
    public static class Node {

        private Opcode opcode;
        private Variable op1;
        private Variable op2;
        private Variable focus;
        private List<Node> predecessors;
        private List<Node> successors;

        // LINK RET JUMP-PREINIT PUSH POP
        public Node(Opcode opcode) {
            this(opcode, null, null, null);
        }

        // JUMP LABEL READI READF WRITEI WRITEF JSR
        public Node(Opcode opcode, Variable focus) {
            this(opcode, null, null, focus);
        }

        // STOREI STOREF
        public Node(Opcode opcode, Variable op1, Variable focus) {
            this(opcode, op1, null, focus);
        }

        public Node(Opcode opcode, Variable op1, Variable op2, Variable focus) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.focus = focus;
            this.predecessors = new ArrayList<>();
            this.successors = new ArrayList<>();
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
            return Conditional.contains(opcode);
        }

        public boolean isJump() {
            return opcode == Opcode.JUMP;
        }

        public boolean isRet() {
            return opcode == Opcode.RET;
        }

    }

    public IR() {
        super();
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
        b.append(";IR CFG\n");
        stream().map(n -> n + ":\n;Predecessors:" + n.getPredecessors() + "\nSuccessors:" + n.getSuccessors() + "\n\n")
                .forEach(b::append);
        return b.toString();
    }

    public static Opcode parseCalcOp(String operator, Variable.Type type) {
        switch (operator) {
            case "+":
                return type == Variable.Type.INT ? Opcode.ADDI : Opcode.ADDF;
            case "-":
                return type == Variable.Type.INT ? Opcode.SUBI : Opcode.SUBF;
            case "*":
                return type == Variable.Type.INT ? Opcode.MULTI : Opcode.MULTF;
            case "/":
                return type == Variable.Type.INT ? Opcode.DIVI : Opcode.DIVF;
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

}
