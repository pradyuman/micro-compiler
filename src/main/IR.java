package main;

import lombok.Data;

import java.util.EnumSet;
import java.util.LinkedList;

/**
 * Intermediate Representation
 */
public class IR extends LinkedList<IR.Node> {

    public enum Opcode {
        ADDI, ADDF, SUBI, SUBF, MULTI, MULTF, DIVI, DIVF,
        STOREI, STOREF, GT, GE, LT, LE, NE, EQ, JUMP, LABEL,
        READI, READF, WRITEI, WRITEF, WRITES,
        JSR, PUSH, POP, RET, LINK
    }

    @Data
    public static class Node {

        public enum Type {
            CALC, COMP, STORE, JUMP, LABEL, SYS
        }

        private static EnumSet<Opcode> CalcSet = EnumSet.of(
                Opcode.ADDI, Opcode.ADDF, Opcode.SUBI, Opcode.SUBF,
                Opcode.MULTI, Opcode.MULTF, Opcode.DIVI, Opcode.DIVF
        );

        private static EnumSet<Opcode> CompSet = EnumSet.of(
                Opcode.GT, Opcode.GE, Opcode.LT, Opcode.LE, Opcode.NE, Opcode.EQ
        );

        private static EnumSet<Opcode> StoreSet = EnumSet.of(
                Opcode.STOREI, Opcode.STOREF
        );

        private static EnumSet<Opcode> SysSet = EnumSet.of(
                Opcode.READI, Opcode.READF, Opcode.WRITEI, Opcode.WRITEF
        );

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
            String s = opcode.toString();
            if (op1 != null) s += " " + op1.getRef();
            if (op2 != null) s += " " + op2.getRef();
            if (focus != null) s += " " + focus.getRef();
            return s;
        }

        public Type getType() {
            if (CalcSet.contains(opcode)) return Type.CALC;
            if (CompSet.contains(opcode)) return Type.COMP;
            if (StoreSet.contains(opcode)) return Type.STORE;
            if (SysSet.contains(opcode)) return Type.SYS;
            if (opcode == Opcode.JUMP) return Type.JUMP;
            if (opcode == Opcode.LABEL) return Type.LABEL;

            throw new MicroException(MicroErrorMessages.UnknownIRNodeType);
        }

        public boolean isRet() {
            return opcode == Opcode.RET;
        }

    }

    public IR() {
        super();
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
