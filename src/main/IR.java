package main;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;

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

    @Data
    @AllArgsConstructor
    public static class Node {

        private Opcode opcode;
        private Variable op1;
        private Variable op2;
        private Variable focus;

        // LINK RET JUMP-PREINIT PUSH POP
        public Node(Opcode opcode) {
            this.opcode = opcode;
        }

        // JUMP LABEL READI READF WRITEI WRITEF JSR
        public Node(Opcode opcode, Variable focus) {
            this(opcode, null, null, focus);
        }

        // STOREI STOREF
        public Node(Opcode opcode, Variable op1, Variable focus) {
            this(opcode, op1, null, focus);
        }

        @Override
        public String toString() {
            String s = opcode.toString();
            if (op1 != null) s += " " + op1.getRef();
            if (op2 != null) s += " " + op2.getRef();
            if (focus != null) s += " " + focus.getRef();
            return s;
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
        b.append(String.format(";IR code\n"));
        stream().map(n -> ";" + n + "\n").forEach(b::append);
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
                throw new MicroException(MicroErrorMessages.UnknownCalcOp);
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
                throw new MicroException(MicroErrorMessages.UnknownCompOp);
        }
    }

}
