package main;

import main.utils.Expression;
import main.utils.TinyTranslator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MicroCustomListener extends MicroBaseListener {

    private int blocknum;
    private int register;
    private List<IR.Node> ir;
    private Stack<Integer> scope;
    private List<SymbolMap> symbolMaps;

    public MicroCustomListener() {
        this.blocknum = 1;
        this.register = 1;
        this.ir = new IR();
        this.scope = new Stack<>();
        this.symbolMaps = new ArrayList<>();
    }

    private SymbolMap lastMap() {
        return symbolMaps.get(symbolMaps.size()-1);
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
        symbolMaps.add(new SymbolMap("GLOBAL"));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        //symbolMaps.forEach(m -> System.out.println(m));
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
        symbolMaps.add(new SymbolMap("BLOCK " + blocknum));
        scope.push(symbolMaps.size()-1);
        blocknum++;
    }

    @Override
    public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolMaps.remove(symbolMaps.size()-1);
        scope.pop();
        blocknum--;
    }

    @Override
    public void enterElse_part(MicroParser.Else_partContext ctx) {
        symbolMaps.add(new SymbolMap("BLOCK " + blocknum));
        scope.push(symbolMaps.size()-1);
        blocknum++;
    }

    @Override
    public void exitElse_part(MicroParser.Else_partContext ctx) {
        scope.pop();
    }

    @Override
    public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        symbolMaps.add(new SymbolMap("BLOCK " + blocknum));
        scope.push(symbolMaps.size()-1);
        blocknum++;
    }

    @Override
    public void exitDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        scope.pop();
    }

    @Override
    public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {
        Variable var = getScopedVariable(ctx.getChild(0).getText());
        // (TODO) Do some better error checking for var == null (throw exception)
        if (var == null) return;

        List<Expression.Token> infix = Expression.tokenizeExpr(ctx.getChild(2).getText());
        infix.add(0, new Expression.Operator(":="));
        infix.add(0, new Expression.Token(Expression.Token.Type.VAR, var.getName()));
        List<Expression.Token> postfix = Expression.transformToPostfix(infix);
        Expression.BENode tree = Expression.generateExpressionTree(postfix);

        tree.forEach(n -> {
            if (n.getToken().isOperator()) {
                IR.Opcode opcode;
                Variable op1, op2;
                Expression.Token token;
                Expression.Operator operator = (Expression.Operator)n.getToken();
                if (operator.getValue() != ":=") {
                    operator.setRegister(register++);
                }

                token = n.getLeft().getToken();
                if (token.isOperator()) {
                    Expression.Operator top = (Expression.Operator)token;
                    op1 = new Variable("$T" + top.getRegister(), var.getType(), true);
                } else {
                    op1 = resolveId(token.getValue());
                }
                // (TODO) Better error - variable does not exist in program (throw exception)
                if (op1 == null) return;
                if (op1.isConstant()) {
                    opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
                    Variable temp = new Variable("$T" + register++, op1.getType(), true);
                    ir.add(new IR.Node(opcode, op1, temp));
                    op1 = temp;
                }

                token = n.getRight().getToken();
                if (token.isOperator()) {
                    Expression.Operator top = (Expression.Operator)token;
                    op2 = new Variable("$T" + top.getRegister(), var.getType(), true);
                } else {
                    op2 = resolveId(token.getValue());
                }
                // (TODO) Better error - variable does not exist in program (throw exception)
                if (op2 == null) return;
                if (op2.isConstant()) {
                    opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
                    Variable temp = new Variable("$T" + register++, op2.getType(), true);
                    ir.add(new IR.Node(opcode, op2, temp));
                    op2 = temp;
                }

                // Can strings be operated on?
                switch (operator.getValue()) {
                    case ":=":
                        opcode = var.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
                        ir.add(new IR.Node(opcode, op2, var));
                        break;
                    case "+":
                        opcode = var.isInt() ? IR.Opcode.ADDI : IR.Opcode.ADDF;
                        ir.add(new IR.Node(opcode, op1, op2,
                                new Variable("$T" + operator.getRegister(), var.getType(), true)));
                        break;
                    case "-":
                        opcode = var.isInt() ? IR.Opcode.SUBI : IR.Opcode.SUBF;
                        ir.add(new IR.Node(opcode, op1, op2,
                                new Variable("$T" + operator.getRegister(), var.getType(), true)));
                        break;
                    case "*":
                        opcode = var.isInt() ? IR.Opcode.MULTI : IR.Opcode.MULTF;
                        ir.add(new IR.Node(opcode, op1, op2,
                                new Variable("$T" + operator.getRegister(), var.getType(), true)));
                        break;
                    case "/":
                        opcode = var.isInt() ? IR.Opcode.DIVI : IR.Opcode.DIVF;
                        ir.add(new IR.Node(opcode, op1, op2,
                                new Variable("$T" + operator.getRegister(), var.getType(), true)));
                        break;
                }
            }
        });
    }

    private Variable resolveId(String id) {
        Variable var = getScopedVariable(id);
        if (var == null) {
            var = Variable.generateConstant(id);
        }
        return var;
    }

    @Override
    public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            // (TODO) Do some better error checking for var == null (throw exception)
            if (var == null) return;
            IR.Opcode opcode = var.isInt() ? IR.Opcode.READI : IR.Opcode.READF;
            ir.add(new IR.Node(opcode, var));
        }
    }

    @Override
    public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = getScopedVariable(s);
            // (TODO) Do some error checking for var == null (throw exception)
            if (var == null) return;
            IR.Opcode opcode = var.isInt() ? IR.Opcode.WRITEI : IR.Opcode.WRITEF;
            ir.add(new IR.Node(opcode, var));
        }
    }

}
