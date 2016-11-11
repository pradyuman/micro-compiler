package main;

import java.util.LinkedList;

/**
 * Intermediate Representation
 */
public class IR extends LinkedList<IR.Node> {

    public enum Opcode {
        ADDI, ADDF, SUBI, SUBF, MULTI, MULTF, DIVI, DIVF,
        STOREI, STOREF, GT, GE, LT, LE, NE, EQ, JUMP, LABEL,
        READI, READF, WRITEI, WRITEF
    }

    public static class Node {

        private Opcode opcode;
        private Variable op1;
        private Variable op2;
        private Variable focus;

        // JUMP LABEL READI READF WRITEI WRITEF
        public Node(Opcode opcode, Variable focus) {
            this.opcode = opcode;
            this.focus = focus;
        }

        // STOREI STOREF
        public Node(Opcode opcode, Variable op1, Variable focus) {
            this.opcode = opcode;
            this.op1 = op1;
            this.focus = focus;
        }

        // ADDI ADDF SUBI SUBF MULTI MULTF GT GE LT LE NE EQ
        public Node(Opcode opcode,
                    Variable op1,
                    Variable op2,
                    Variable focus) {
            this.opcode = opcode;
            this.op1 = op1;
            this.op2 = op2;
            this.focus = focus;
        }

        @Override
        public String toString() {
            String s = opcode + " ";
            if (op1 != null) s += op1.isConstant() ? op1.getValue() + " " : op1.getName() + " ";
            if (op2 != null) s += op2.isConstant() ? op2.getValue() + " " : op2.getName() + " ";
            return s + focus.getName();
        }

        public Opcode getOpcode() {
            return opcode;
        }

        public Variable getOp1() {
            return op1;
        }

        public Variable getOp2() {
            return op2;
        }

        public Variable getFocus() {
            return focus;
        }

        public void setFocus(Variable focus) {
            this.focus = focus;
        }

    }

    public IR() {
        super();
    }

}
