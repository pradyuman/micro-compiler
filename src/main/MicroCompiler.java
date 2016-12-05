package main;

import java.util.*;
import java.util.stream.IntStream;

import main.utils.Expression;
import main.utils.TinyTranslator;
import org.antlr.v4.runtime.tree.ParseTree;

public class MicroCompiler extends MicroBaseListener {

    private static final String BLOCK = "BLOCK";
    private static final String GLOBAL = "GLOBAL";
    private static final String LABEL_PREFIX = "label";

    // Set if in a function scope
    private boolean inFunction;

    // Tracks local variables in functions
    private int flocalnum;

    // Tracks functions parameters
    private int fparamnum;

    // Tracks program blocks
    private int blocknum;

    // Tracks label numbers
    private int labelnum;

    // Tracks temporary registers
    private int register;

    // Intermediate Representation of program
    private IR ir;

    // Tracks program scope
    private LinkedList<Integer> scope;

    // Tracks label scope
    private Deque<Integer> labelScope;

    // Defers jump nodes for conditionals
    private Deque<IR.Node> defer;

    // Defers function parameter naming
    private Deque<Variable> deferParam;

    // Conditional label map to make ir node lookups easier when generating CFG
    private Map<String, IR.Node> condLabelMap;

    // Program symbol maps
    private List<SymbolMap> symbolMaps;

    public MicroCompiler() {
        this.inFunction = false;
        this.flocalnum = 1;
        this.fparamnum = 0;
        this.blocknum = 1;
        this.labelnum = 1;
        this.register = 1;
        this.ir = new IR();
        this.scope = new LinkedList<>();
        this.labelScope = new ArrayDeque<>();
        this.defer = new ArrayDeque<>();
        this.deferParam = new ArrayDeque<>();
        this.condLabelMap = new HashMap<>();
        this.symbolMaps = new ArrayList<>();
    }

    private SymbolMap lastMap() {
        return symbolMaps.get(symbolMaps.size()-1);
    }

    private String nextBlockName() {
        return BLOCK + " " + blocknum++;
    }

    private Variable getScopedVariable(String id) {
        return scope.stream()
                .map(s -> symbolMaps.get(s).get(id))
                .filter(s -> s != null)
                .findFirst().orElse(null);
    }

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        scope.push(0);
        symbolMaps.add(new SymbolMap(GLOBAL));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        generateCFG();
        generateInAndOut();
        System.out.println(ir);
        TinyTranslator tt = new TinyTranslator();
        tt.printTinyFromIR(symbolMaps.get(0), ir);
    }

    private void generateCFG() {
        IntStream.range(0, ir.size()).forEach(i -> {
            IR.Node node = ir.get(i);
            IR.Node next = getNextIRNode(i);
            if (node.isConditional()) {
                resolveCFGInfo(node, next);
                IR.Node target = getCFTarget(node);
                if (next == null || !target.getFocus().getName().equals(next.getFocus().getName()))
                    resolveCFGInfo(node, target);
            } else if (node.isJump()) {
                resolveCFGInfo(node, getCFTarget(node));
            } else if (!node.isRet()) {
                resolveCFGInfo(node, next);
            }
        });
    }

    private void generateInAndOut() {
        IntStream.range(0, ir.size()).map(i -> ir.size() - i - 1).forEach(i -> {
            IR.Node node = ir.get(i);
            IR.Node next = getNextIRNode(i);

            // Generate In
            if (next != null) {
                next.getSuccessors().stream()
                        .map(s -> s.getIn())
                        .forEach(node.getOut()::addAll);
            }

            // Generate Out
            Set<Variable> outCopy = new HashSet<>(node.getOut());
            outCopy.removeAll(node.getKill());
            node.getIn().addAll(outCopy);
            node.getIn().addAll(node.getGen());
        });
    }

    private IR.Node getNextIRNode(int i) {
        if (i + 1 < ir.size())
            return ir.get(i + 1);
        return null;
    }

    private void resolveCFGInfo(IR.Node node, IR.Node successor) {
        if (successor == null || node == null)
            return;

        node.getSuccessors().add(successor);
        successor.getPredecessors().add(node);
    }

    private IR.Node getCFTarget(IR.Node node) {
        IR.Node target = condLabelMap.get(node.getFocus().getName());
        if (target == null)
            throw new MicroRuntimeException(MicroErrorMessages.UnableToFindBranchTarget);
        return target;
    }

    @Override
    public void enterString_decl(MicroParser.String_declContext ctx) {
        String name = ctx.getChild(1).getText();
        Variable var = new Variable(name, Variable.Type.STRING, ctx.getChild(3).getText());
        symbolMaps.get(symbolMaps.size()-1).put(name, var);
    }

    @Override
    public void enterVar_decl(MicroParser.Var_declContext ctx) {
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);

        for (String s : ctx.getChild(1).getText().split(",")) {
            Variable var = inFunction
                    ? new Variable(Variable.Context.FLOCAL, flocalnum++, s, type)
                    : new Variable(s, type);
            lastMap().put(s, var);
        }
    }

    @Override
    public void exitParam_decl_list(MicroParser.Param_decl_listContext ctx) {
        fparamnum = deferParam.size();
        lastMap().setNumParam(deferParam.size());
        IntStream.rangeClosed(1, deferParam.size()).forEach(i -> deferParam.pop().setCtxVal(i));
    }

    @Override
    public void enterParam_decl(MicroParser.Param_declContext ctx) {
        String name = ctx.getChild(1).getText();
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);

        Variable fparam = new Variable(Variable.Context.FPARAM, 0, name, type);
        lastMap().put(name, fparam);
        deferParam.push(fparam);
    }

    @Override
    public void enterFunc_decl(MicroParser.Func_declContext ctx) {
        inFunction = true;

        String name = ctx.getChild(2).getText();
        symbolMaps.add(new SymbolMap(name, true));
        scope.push(symbolMaps.size()-1);

        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(name, Variable.Type.STRING)));

        IR.Node link = new IR.Node(IR.Opcode.LINK);
        ir.add(link);
        defer.push(link);
    }

    @Override
    public void exitFunc_decl(MicroParser.Func_declContext ctx) {
        scope.pop();
        defer.pop().setFocus(new Variable(Integer.toString(flocalnum - 1), Variable.Type.STRING));
        inFunction = false;
        flocalnum = 1;
        fparamnum = 0;
        register = 1;
    }

    @Override
    public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {
        Variable var = getScopedVariable(ctx.getChild(0).getText());
        if (var == null)
            throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable);

        Variable focus = parseExpr(ctx.getChild(2).getText());
        IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, focus, var));
    }

    @Override
    public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            if (var == null)
                throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable);

            IR.Opcode opcode = var.isInt() ? IR.Opcode.READI : IR.Opcode.READF;
            ir.add(new IR.Node(opcode, var));
        }
    }

    @Override
    public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            if (var == null)
                throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable);

            IR.Opcode opcode;
            switch (var.getType()) {
                case INT:
                    opcode = IR.Opcode.WRITEI; break;
                case FLOAT:
                    opcode = IR.Opcode.WRITEF; break;
                default:
                    opcode = IR.Opcode.WRITES;
            }

            ir.add(new IR.Node(opcode, var));
        }
    }

    @Override
    public void enterReturn_stmt(MicroParser.Return_stmtContext ctx) {
        Variable focus = parseExpr(ctx.getChild(1).getText());
        IR.Opcode opcode = focus.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, focus,
                new Variable(Variable.Context.RETURN, fparamnum + 1, null, focus.getType())));
        ir.add(new IR.Node(IR.Opcode.RET));
    }

    @Override
    public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size()-1);

        parseCond(ctx.getChild(2), LABEL_PREFIX + labelnum, true);
        labelScope.push(labelnum++);
        defer.push(new IR.Node(IR.Opcode.JUMP));
    }

    @Override
    public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        defer.pop().setFocus(resolveLabel(labelScope.pop()));
    }

    @Override
    public void enterElse_part(MicroParser.Else_partContext ctx) {
        if (ctx.getChild(0) == null) return;

        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size() - 1);

        if (!ir.get(ir.size()-1).isRet())
            ir.add(defer.peek());

        resolveLabel(labelScope.pop());
        labelScope.push(labelnum++);
        parseCond(ctx.getChild(2), LABEL_PREFIX + labelScope.peek(), true);
    }

    @Override
    public void exitElse_part(MicroParser.Else_partContext ctx) {
        scope.pop();
    }

    @Override
    public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size()-1);

        resolveLabel(labelnum);
        labelScope.push(labelnum++);
    }

    @Override
    public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        parseCond(ctx.getChild(5), LABEL_PREFIX + labelScope.pop(), false);
        scope.pop();
    }

     private Variable resolveLabel(int num) {
        String labelName = LABEL_PREFIX + num;
        Variable labelVar = new Variable(labelName, Variable.Type.STRING);
        IR.Node labelNode = new IR.Node(IR.Opcode.LABEL, labelVar);
        ir.add(labelNode);
        condLabelMap.put(labelName, labelNode);
        return labelVar;
    }

    public void parseCond(ParseTree cond, String label, boolean opposite) {
        Variable target = new Variable(label, Variable.Type.STRING);

        switch (cond.getText()) {
            case "TRUE":
                if (!opposite)
                    ir.add(new IR.Node(IR.Opcode.JUMP, target));
                break;
            case "FALSE":
                if (opposite)
                    ir.add(new IR.Node(IR.Opcode.JUMP, target));
                break;
            default:
                Variable left = parseExpr(cond.getChild(0).getText());
                Variable right = parseExpr(cond.getChild(2).getText());
                IR.Opcode opcode = IR.parseCompOp(cond.getChild(1).getText(), opposite);
                ir.add(new IR.Node(opcode, left, right, target));
        }
    }

    // Returns last relevant variable on IR
    public Variable parseExpr(String expr) {
        List<Expression.Token> infix = Expression.tokenizeExpr(expr, symbolMaps);
        List<Expression.Token> postfix = Expression.transformToPostfix(infix);
        Expression.ENode tree = Expression.generateExpressionTree(postfix);

        // Expression is a single constant
        if (tree.getToken().isVar())
            return resolveToken(tree.getToken());

        // Expression includes an operator
        tree.postorder().forEach(n -> {
            if (n.getToken().isFunction()) {
                ir.add(new IR.Node(IR.Opcode.PUSH));
                n.forEach(p -> ir.add(new IR.Node(IR.Opcode.PUSH, resolveToken(p.getToken()))));
                ir.add(new IR.Node(IR.Opcode.JSR,
                        new Variable(n.getToken().getValue(), Variable.Type.STRING)));
                n.forEach(p -> ir.add(new IR.Node(IR.Opcode.POP)));
                ir.add(new IR.Node(IR.Opcode.POP,
                        new Variable(Variable.Context.TEMP, register++, null, null)));
            }

            if (n.getToken().isOperator()) {
                Expression.Operator operator = (Expression.Operator)n.getToken();
                operator.setRegister(register++);

                Variable op1 = resolveToken(n.get(0).getToken());
                Variable op2 = resolveToken(n.get(1).getToken());
                Variable.Type exprType = op1.isFloat() || op2.isFloat() ? Variable.Type.FLOAT : Variable.Type.INT;
                Variable result = new Variable(Variable.Context.TEMP, operator.getRegister(), null, exprType);
                ir.add(new IR.Node(IR.parseCalcOp(operator.getValue(), exprType), op1, op2, result));
            }
        });

        return ir.get(ir.size() - 1).getFocus();
    }

    private Variable resolveToken(Expression.Token token) {
        Variable var = tokenToVariable(token);
        if (var == null)
            throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable);

        if (var.isConstant()) {
            IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
            Variable temp = new Variable(Variable.Context.TEMP, register++, null, var.getType());
            ir.add(new IR.Node(opcode, var, temp));
            return temp;
        }

        return var;
    }

    private Variable tokenToVariable(Expression.Token token) {
        if (token.isOperator())
            return new Variable(Variable.Context.TEMP, ((Expression.Operator)token).getRegister(), null, null);

        return resolveId(token.getValue());
    }

    private Variable resolveId(String id) {
        Variable var = getScopedVariable(id);

        if (var == null)
            var = Variable.parseConstant(id);

        return var;
    }

}