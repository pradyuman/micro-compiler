package main;

import main.utils.Expression;
import main.utils.TinyTranslator;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MicroCustomListener extends MicroBaseListener {

    private static String BLOCK = "BLOCK";
    private static String GLOBAL = "GLOBAL";
    private static String LABEL_PREFIX = "label";

    private boolean inFunction;
    private int flocalnum;
    private int fparamnum;
    private int blocknum;
    private int labelnum;
    private int register;
    private List<IR.Node> ir;
    private Stack<Integer> scope;
    private Stack<Integer> lcstack;
    private Stack<IR.Node> iejump;
    private List<SymbolMap> symbolMaps;

    public MicroCustomListener() {
        this.inFunction = false;
        this.flocalnum = 1;
        this.fparamnum = 1;
        this.blocknum = 1;
        this.labelnum = 1;
        this.register = 1;
        this.ir = new IR();
        this.scope = new Stack<>();
        this.lcstack = new Stack<>();
        this.iejump = new Stack<>();
        this.symbolMaps = new ArrayList<>();
    }

    private SymbolMap lastMap() {
        return symbolMaps.get(symbolMaps.size()-1);
    }

    private String nextBlockName() {
        return BLOCK + " " + blocknum++;
    }

    private Variable getScopedVariable(String id) {
        Variable var = symbolMaps.get(scope.peek()).get(id);
        for (int i = scope.size()-2; i >= 0 && var == null; i--) {
            var = symbolMaps.get(scope.get(i)).get(id);
        }
        return var;
    }

    public boolean isFunction(String s) {
        return symbolMaps.stream().anyMatch(m -> m.getName().equals(s));
    }

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        scope.push(0);
        symbolMaps.add(new SymbolMap(GLOBAL));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        //symbolMaps.forEach(m -> System.out.println(";" + m));

        ir.stream().map(n -> ";" + n).forEach(System.out::println);
        //TinyTranslator tt = new TinyTranslator();
        //tt.printTinyFromIR(symbolMaps, ir);
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
                    ? new Variable(s, type, Variable.Context.FLOCAL, flocalnum++)
                    : new Variable(s, type);
            lastMap().put(s, var);
        }
    }

    @Override
    public void exitParam_decl_list(MicroParser.Param_decl_listContext ctx) {
        lastMap().setNumParam(fparamnum - 1);
        fparamnum = 1;
    }

    @Override
    public void enterParam_decl(MicroParser.Param_declContext ctx) {
        String name = ctx.getChild(1).getText();
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);
        lastMap().put(name, new Variable(name, type, Variable.Context.FPARAM, fparamnum++));
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
        ir.add(new IR.Node(IR.Opcode.LINK));
    }

    @Override
    public void exitFunc_decl(MicroParser.Func_declContext ctx) {
        scope.pop();
        inFunction = false;
        flocalnum = 1;
        register = 1;
    }

    @Override
    public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {
        Variable var = getScopedVariable(ctx.getChild(0).getText());
        if (var == null)
            throw new MicroException(MicroErrorMessages.UndefinedVariable);

        Variable focus = parseExpr(ctx.getChild(2).getText());
        IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, focus, var));
    }

    @Override
    public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            if (var == null)
                throw new MicroException(MicroErrorMessages.UndefinedVariable);

            IR.Opcode opcode = var.isInt() ? IR.Opcode.READI : IR.Opcode.READF;
            ir.add(new IR.Node(opcode, var));
        }
    }

    @Override
    public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            if (var == null)
                throw new MicroException(MicroErrorMessages.UndefinedVariable);

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
                new Variable(focus.getType(), Variable.Context.RETURN)));
        ir.add(new IR.Node(IR.Opcode.RET));
    }

    @Override
    public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size()-1);

        parseCond(ctx.getChild(2), LABEL_PREFIX + labelnum, true);
        lcstack.push(labelnum++);
        iejump.push(new IR.Node(IR.Opcode.JUMP));
    }

    @Override
    public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(LABEL_PREFIX + lcstack.peek(), Variable.Type.STRING)
        ));
        iejump.pop().setFocus(new Variable(LABEL_PREFIX + lcstack.pop(), Variable.Type.STRING));
    }

    @Override
    public void enterElse_part(MicroParser.Else_partContext ctx) {
        if (ctx.getChild(0) == null) return;

        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size() - 1);

        if (!ir.get(ir.size()-1).isRet())
            ir.add(iejump.peek());

        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(LABEL_PREFIX + lcstack.pop(), Variable.Type.STRING)
        ));

        lcstack.push(labelnum++);
        parseCond(ctx.getChild(2), LABEL_PREFIX + lcstack.peek(), true);
    }

    @Override
    public void exitElse_part(MicroParser.Else_partContext ctx) {
        scope.pop();
    }

    @Override
    public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size()-1);

        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(LABEL_PREFIX + labelnum, Variable.Type.STRING)
        ));
        lcstack.push(labelnum++);
    }

    @Override
    public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        parseCond(ctx.getChild(5), LABEL_PREFIX + lcstack.pop(), false);
        scope.pop();
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
                        new Variable(null, null, Variable.Context.TEMP, register++)));
            }

            if (n.getToken().isOperator()) {
                Expression.Operator operator = (Expression.Operator)n.getToken();
                operator.setRegister(register++);

                Variable op1 = resolveToken(n.get(0).getToken());
                Variable op2 = resolveToken(n.get(1).getToken());
                Variable.Type exprType = op1.isFloat() || op2.isFloat() ? Variable.Type.FLOAT : Variable.Type.INT;
                Variable result = new Variable(null, exprType, Variable.Context.TEMP, operator.getRegister());
                ir.add(new IR.Node(IR.parseCalcOp(operator.getValue(), exprType), op1, op2, result));
            }
        });

        return ir.get(ir.size() - 1).getFocus();
    }

    private Variable resolveToken(Expression.Token token) {
        Variable var = tokenToVariable(token);
        if (var == null)
            throw new MicroException(MicroErrorMessages.UndefinedVariable);

        if (var.isConstant()) {
            IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
            Variable temp = new Variable(null, var.getType(), Variable.Context.TEMP, register++);
            ir.add(new IR.Node(opcode, var, temp));
            return temp;
        }

        return var;
    }

    private Variable tokenToVariable(Expression.Token token) {
        if (token.isOperator())
            return new Variable(null, null, Variable.Context.TEMP, ((Expression.Operator)token).getRegister());

        return resolveId(token.getValue());
    }

    private Variable resolveId(String id) {
        Variable var = getScopedVariable(id);

        if (var == null)
            var = Variable.parseConstant(id);

        return var;
    }

}