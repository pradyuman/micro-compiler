package main;

import main.utils.Expression;
import main.utils.TinyTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MicroCustomListener extends MicroBaseListener {

    private static String BLOCK = "BLOCK";
    private static String GLOBAL = "GLOBAL";
    private static String LABEL_PREFIX = "label";
    private static String TEMPREG_PREFIX = "$T";

    private int blocknum;
    private int labelnum;
    private int register;
    private List<IR.Node> ir;
    private Stack<Integer> scope;
    private Stack<Expression.BENode> exprstack;
    private Stack<Integer> lcstack;
    private Stack<IR.Node> iejump;
    private List<SymbolMap> symbolMaps;

    public MicroCustomListener() {
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

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        scope.push(0);
        symbolMaps.add(new SymbolMap(GLOBAL));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        ir.forEach(n -> System.out.println(";" + n));
        TinyTranslator tt = new TinyTranslator();
        tt.printTinyFromIR(symbolMaps, ir);
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
            lastMap().put(s, new Variable(s, type, null));
        }
    }

    @Override
    public void enterParam_decl(MicroParser.Param_declContext ctx) {
        String name = ctx.getChild(1).getText();
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);
        lastMap().put(name, new Variable(name, type, null));
    }

    @Override
    public void enterFunc_decl(MicroParser.Func_declContext ctx) {
        symbolMaps.add(new SymbolMap(ctx.getChild(2).getText()));
        scope.push(symbolMaps.size()-1);
    }

    @Override
    public void exitFunc_decl(MicroParser.Func_declContext ctx) {
        scope.pop();
    }

    @Override
    public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size()-1);

        ir.add(new IR.Node(
                IR.Opcode.EQ,
                new Variable(LABEL_PREFIX + labelnum, Variable.Type.STRING, null)
        ));
        lcstack.push(labelnum++);

        iejump.push(new IR.Node(IR.Opcode.JUMP, null));
    }

    @Override
    public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(LABEL_PREFIX + lcstack.peek(), Variable.Type.STRING, null)
        ));
        iejump.pop().setFocus(new Variable(LABEL_PREFIX + lcstack.pop(), Variable.Type.STRING, null));
    }

    @Override
    public void enterElse_part(MicroParser.Else_partContext ctx) {
        if (ctx.getChild(0) == null) return;

        symbolMaps.add(new SymbolMap(nextBlockName()));
        scope.push(symbolMaps.size() - 1);

        ir.add(iejump.peek());
        ir.add(new IR.Node(
                IR.Opcode.LABEL,
                new Variable(LABEL_PREFIX + lcstack.pop(), Variable.Type.STRING, null)
        ));

        lcstack.push(labelnum++);
        ir.add(new IR.Node(
                IR.Opcode.NE,
                new Variable(LABEL_PREFIX + lcstack.peek(), Variable.Type.STRING, null)
        ));
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
                new Variable(LABEL_PREFIX + labelnum, Variable.Type.STRING, null)
        ));
        lcstack.push(labelnum++);
    }

    @Override
    public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        String loopexit = LABEL_PREFIX + lcstack.pop();
        ir.add(new IR.Node(
                IR.Opcode.GE,
                new Variable(loopexit, Variable.Type.STRING, null)
        ));
        scope.pop();
    }

    @Override
    public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {
        Variable var = getScopedVariable(ctx.getChild(0).getText());
        if (var == null)
            throw new MicroException(MicroErrorMessages.UndefinedVariable);

        parseExpr(ctx.getChild(2).getText());
        IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, ir.get(ir.size()-1).getFocus(), var));
    }

    public void parseExpr(String expr) {
        List<Expression.Token> infix = Expression.tokenizeExpr(expr);
        List<Expression.Token> postfix = Expression.transformToPostfix(infix);
        Expression.BENode tree = Expression.generateExpressionTree(postfix);

        // Expression is a single constant
        if (!tree.getToken().isOperator())
            resolveToken(tree.getToken());

        // Expression includes an operator
        tree.forEach(n -> {
            if (n.getToken().isOperator()) {
                Expression.Operator operator = (Expression.Operator)n.getToken();
                operator.setRegister(register++);

                Variable op1 = resolveToken(n.getLeft().getToken());
                Variable op2 = resolveToken(n.getRight().getToken());
                Variable.Type exprType = op1.isFloat() || op2.isFloat() ? Variable.Type.FLOAT : Variable.Type.INT;
                Variable result = new Variable(TEMPREG_PREFIX + operator.getRegister(), exprType, true);
                ir.add(new IR.Node(IR.parseOperator(operator.getValue(), exprType), op1, op2, result));
            }
        });
    }

    private Variable resolveToken(Expression.Token token) {
        Variable var = tokenToVariable(token);

        if (var == null)
            throw new MicroException(MicroErrorMessages.UndefinedVariable);

        if (var.isConstant()) {
            IR.Opcode opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
            Variable temp = new Variable(TEMPREG_PREFIX + register++, var.getType(), true);
            ir.add(new IR.Node(opcode, var, temp));
            return temp;
        }

        return var;
    }

    private Variable tokenToVariable(Expression.Token token) {
        if (token.isOperator())
            return new Variable(TEMPREG_PREFIX + ((Expression.Operator)token).getRegister(), null, true);

        return resolveId(token.getValue());
    }

    private Variable resolveId(String id) {
        Variable var = getScopedVariable(id);

        if (var == null)
            var = Variable.parseConstant(id);

        return var;
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

            IR.Opcode opcode = var.isInt() ? IR.Opcode.WRITEI : IR.Opcode.WRITEF;
            ir.add(new IR.Node(opcode, var));
        }
    }

}