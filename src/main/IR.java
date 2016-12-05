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

    private static EnumSet<Opcode> ConditionalSet = EnumSet.of(
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
        private List<Variable> gen;
        private List<Variable> kill;

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
            this.predecessors = new ArrayList<>(2);
            this.successors = new ArrayList<>(2);
            this.gen = new ArrayList<>(2);
            this.kill = new ArrayList<>(2);
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
        b.append("IR CFG\n");
        stream().map(n -> n + ":\n;Predecessors:" + n.getPredecessors() + "\nSuccessors:" + n.getSuccessors() + "\n\n")
                .forEach(b::append);
        return b.toString();
    }

    public String genAndKillToSting() {
        StringBuilder b = new StringBuilder();
        b.append("IR GEN and KILL sets\n");
        stream().map(n -> n + ":\nGEN:" + n.getGen() + "\nKILL:" + n.getKill() + "\n\n")
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

    // Override to create gen and kill sets
    @Override
    public boolean add(IR.Node node) {
        generateGenAndKill(node);
        return super.add(node);
    }

    private void generateGenAndKill(IR.Node node) {
        switch(node.getOpcode()) {
            case PUSH:
            case WRITEI:
            case WRITEF:
            case WRITES:
                if (node.getFocus() != null && !node.getFocus().isConstant())
                    node.gen.add(node.getFocus());
                return;
            case POP:
            case READI:
            case READF:
                if (node.getFocus() != null && !node.getFocus().isConstant())
                    node.kill.add(node.getFocus());
                return;
            case GT:
            case GE:
            case LT:
            case LE:
            case NE:
            case EQ:
            case JSR:
            case LABEL:
                return;
            default:
                if (node.getOp1() != null && !node.getOp1().isConstant())
                    node.gen.add(node.getOp1());
                if (node.getOp2() != null && !node.getOp2().isConstant())
                    node.gen.add(node.getOp2());
                if(node.getFocus() != null)
                    node.kill.add(node.getFocus());
                return;
        }

    }

}
