package compiler.translator;

import lombok.AllArgsConstructor;
import lombok.Data;
import compiler.IR;
import compiler.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class RegisterFile {

    @Data
    @AllArgsConstructor
    public static class Register {
        private int id;
        private Variable value;
        private boolean dirty;
    }

    List<Register> file;

    RegisterFile(int num) {
        file = new ArrayList<>(num);
        IntStream.range(0, num).forEach(i -> file.add(new Register(i, null, false)));
    }

    public Register ensure(Variable var, IR.Node node, IR tinyIR, int localCount) {
        Register r = get(var);
        if (r != null)
            return r;

        return allocate(var, node, tinyIR, localCount, true);
    }

    public Register allocate(Variable var, IR.Node node, IR tinyIR, int localCount, boolean load) {
        Register notDirty = file.stream()
                .filter(r -> !r.isDirty())
                .findFirst().orElse(null);

        if (notDirty != null) {
            notDirty.setValue(var);
            notDirty.setDirty(true);
            return notDirty;
        }

        // TODO: get last used register
        Register r = chooseFree(node);
        free(var, r, tinyIR, node.getOut(), localCount, load);
        return r;
    }

    private Register chooseFree(IR.Node ins) {
        return file.stream().filter(r -> {
            boolean op1Valid = ins.getOp1() == null || r.getValue().getRef() != ins.getOp1().getRef();
            boolean op2Valid = ins.getOp2() == null || r.getValue().getRef() != ins.getOp2().getRef();
            boolean focusValid = ins.getFocus() == null || r.getValue().getRef() != ins.getFocus().getRef();
            return op1Valid && op2Valid && focusValid;
        }).findFirst().orElse(null);
    }

    public void free(Variable var, Register r, IR tinyIR, Set<Variable> liveSet, int localCount, boolean load) {
        if (r.getValue() == null)
            return;

        boolean live = liveSet.stream()
                .map(v -> v.getRef())
                .anyMatch(ref -> ref.equals(r.getValue().getRef()));

        if (r.isDirty() && live)
            move(var, r, tinyIR, localCount, load);
    }

    private Register get(Variable var) {
        return file.stream()
                .filter(r -> r.getValue() != null)
                .filter(r -> r.getValue().getRef().equals(var.getRef()))
                .findFirst().orElse(null);
    }

    private void move(Variable var, Register r, IR tinyIR, int localCount, boolean load) {
        IR.Opcode opcode = r.getValue().isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;

        // local, param, temp
        int relativeStackAddress;
        switch(var.getCtx()) {
            case TEMP:
                relativeStackAddress = -(localCount + var.getCtxVal()); break;
            case FLOCAL:
                relativeStackAddress = -var.getCtxVal(); break;
            case FPARAM:
                relativeStackAddress = 6 + var.getCtxVal() - 1; break;
            case RETURN:
                // should be parameters
                relativeStackAddress = 0; break;
            default:
                return;
        }

        Variable temp = new Variable(Variable.Context.TEMP, relativeStackAddress, null, var.getType());
        if (load)
            tinyIR.add(new IR.Node(opcode, temp, var));
        else
            tinyIR.add(new IR.Node(opcode, var, temp));

        r.setDirty(false);
    }
}
