package compiler.translator;

import compiler.element.Register;
import compiler.IR;
import compiler.element.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RegisterFile {

    List<Register> file;

    RegisterFile(int num) {
        file = new ArrayList<>(num);
        IntStream.range(0, num).forEach(i -> file.add(new Register(i, null, false)));
    }

    public Register ensure(Element el, IR.Node node, IR tinyIR, int localCount) {
        System.out.println("ensure " + el + " (" + node + ")");
        Register r = get(el);
        if (r != null) {
            System.out.println("got " + r + " for " + el);
            return r;
        }

        r = allocate(el, node, tinyIR, localCount);
        // generate load
        move(el, r, tinyIR, localCount);
        return r;
    }

    public Register allocate(Element el, IR.Node node, IR tinyIR, int localCount) {
        System.out.println("allocate " + el + " (" + node + ")");
        // Check if there is a free register in the register file and return that register
        Register r = file.stream()
                .filter(reg -> reg.getData() == null)
                .findFirst().orElse(null);

        file.stream().forEach(System.out::println);

        // If there were no free registers, choose a register and free it
        if (r == null) {
            r = chooseFree(node);
            free(r, tinyIR, node, localCount);
        }

        r.setData(el);
        r.setType(el.getType());
        System.out.println("got " + r + " for " + el);
        return r;
    }

    private Register chooseFree(IR.Node ins) {
        return file.stream().filter(r -> {
            boolean op1Valid = ins.getOp1() == null || !r.getData().getRef().equals(ins.getOp1().getRef());
            boolean op2Valid = ins.getOp2() == null || !r.getData().getRef().equals(ins.getOp2().getRef());
            return op1Valid && op2Valid;
        }).findFirst().orElse(null);
    }

    public void free(Register r, IR tinyIR, IR.Node node, int localCount) {
        boolean live = node.isElementLive(r.getData());

        System.out.println("freeing " + r + ": " + node.getOut() + " isLive: " + live);
        //generate store if needed
        if (r.isDirty() && live)
            move(r, r.getData(), tinyIR, localCount);

        r.setData(null);
        r.setDirty(false);
    }

    public Register transfer(Register r, Element to, IR tinyIR, IR.Node node, int localCount) {
        free(r, tinyIR, node, localCount);
        r.setData(to);
        return r;
    }

    public Register get(Element var) {
        return file.stream()
                .filter(r -> r.getData() != null)
                .filter(r -> r.getData().getRef().equals(var.getRef()))
                .findFirst().orElse(null);
    }

    private void move(Element from, Element to, IR tinyIR, int localCount) {
        System.out.println("moving " + from + " (" + from.getCtx() + ")");
        IR.Opcode opcode = from.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        tinyIR.add(new IR.Node(opcode, from.getTinyElement(localCount), to.getTinyElement(localCount)));
    }



    public void flush(IR tinyIR, int localCount) {
        System.out.println("flushing");
        file.stream()
                .filter(r -> r.getData() != null)
                .filter(r -> r.isDirty())
                .forEach(r -> {
                    move(r, r.getData(), tinyIR, localCount);
                    r.setData(null);
                    r.setDirty(false);
                });
    }
}
