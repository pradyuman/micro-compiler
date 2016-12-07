package compiler;

import java.util.*;
import java.util.stream.IntStream;

import compiler.element.*;
import compiler.expression.Expression;
import compiler.expression.Operator;
import compiler.expression.Token;
import compiler.translator.TinyTranslator;
import org.antlr.v4.runtime.ParserRuleContext;
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
    private Integer register;

    // Intermediate Representation of program
    private IR ir;

    // Tracks program scope
    private LinkedList<Integer> scope;

    // Tracks label scope
    private Deque<Integer> labelScope;

    // Defers jump nodes for conditionals
    private Deque<IR.Node> defer;

    // Defers function parameter naming
    private Deque<Element> deferParam;

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

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        scope.push(0);
        symbolMaps.add(new SymbolMap(GLOBAL));
        this.ir = new IR(lastMap());
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        //generateCFG();
        //while (!generateInAndOut());
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
            } else if (!node.isReturn()) {
                resolveCFGInfo(node, next);
            }
        });
    }

    private boolean generateInAndOut() {
        boolean converged = true;
        for (int i = ir.size() - 1; i >= 0; i--) {
            IR.Node node = ir.get(i);
            Set<Element> curIn = new LinkedHashSet<>(node.getIn());
            Set<Element> curOut = new LinkedHashSet<>(node.getOut());

            if (node.isReturn() && i != ir.size() - 1)
                symbolMaps.get(0).values().stream().forEach(node.getOut()::add);

            // Generate Out
            node.getSuccessors().stream()
                    .map(s -> s.getIn())
                    .forEach(node.getOut()::addAll);

            // Generate In
            Set<Element> outCopy = new HashSet<>(node.getOut());
            outCopy.removeAll(node.getKill());
            node.getIn().addAll(outCopy);
            node.getIn().addAll(node.getGen());

            if (!curIn.equals(node.getIn()) || !curOut.equals(node.getOut()))
                converged = false;
        }
        return converged;
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
        Element var = new Variable(name, Element.Type.STRING, ctx.getChild(3).getText());
        symbolMaps.get(symbolMaps.size()-1).put(name, var);
    }

    @Override
    public void enterVar_decl(MicroParser.Var_declContext ctx) {
        String rawtype = ctx.getChild(0).getText();
        Element.Type type = Element.Type.valueOf(rawtype);

        for (String s : ctx.getChild(1).getText().split(",")) {
            Element var = inFunction
                    ? new FunctionLocal(flocalnum++, s, type)
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
        Element.Type type = Element.Type.valueOf(rawtype);

        Element fparam = new FunctionParameter(0, name, type);
        lastMap().put(name, fparam);
        deferParam.push(fparam);
    }

    @Override
    public void enterFunc_decl(MicroParser.Func_declContext ctx) {
        inFunction = true;

        String name = ctx.getChild(2).getText();
        symbolMaps.add(new SymbolMap(name, true));
        scope.push(symbolMaps.size()-1);
        ir.add(new IR.Node(IR.Opcode.LABEL, new Label(name)));

        IR.Node link = new IR.Node(IR.Opcode.LINK);
        ir.add(link);
        defer.push(link);
    }

    @Override
    public void exitFunc_decl(MicroParser.Func_declContext ctx) {
        // Set LINK number (#local + #temp)
        defer.pop().setFocus(new Link(flocalnum - 1, register));
        scope.pop();
        inFunction = false;
        flocalnum = 1;
        fparamnum = 0;
        register = 1;
    }

    @Override
    public void enterAssign_expr(MicroParser.Assign_exprContext ctx) {
        Element el = getElementSafely(ctx, ctx.getChild(0).getText());
        Element focus = parseExpr(ctx.getChild(2).getText());
        IR.Opcode opcode = el.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, focus, el));
    }

    @Override
    public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Element el = getElementSafely(ctx, s);
            IR.Opcode opcode = el.isInt() ? IR.Opcode.READI : IR.Opcode.READF;
            ir.add(new IR.Node(opcode, el));
        }
    }

    @Override
    public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            IR.Opcode opcode;
            Element var = getElementSafely(ctx, s);
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

    private Element getElementSafely(ParserRuleContext ctx, String name) {
        Element e = Element.getScopedElement(symbolMaps, scope, name);
        if (e == null) {
            String meta = " " + name + " (" + ctx.getStart().getLine() + ")";
            throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable + meta);
        }
        return e;
    }

    @Override
    public void enterReturn_stmt(MicroParser.Return_stmtContext ctx) {
        Element focus = parseExpr(ctx.getChild(1).getText());
        IR.Opcode opcode = focus.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
        ir.add(new IR.Node(opcode, focus, new Return(fparamnum, focus.getType())));
        ir.add(new IR.Node(IR.Opcode.RETURN));
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

        if (!ir.get(ir.size()-1).isReturn())
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

     private Element resolveLabel(int num) {
        String labelName = LABEL_PREFIX + num;
        Element labelVar = new Label(labelName);
        IR.Node labelNode = new IR.Node(IR.Opcode.LABEL, labelVar);
        ir.add(labelNode);
        condLabelMap.put(labelName, labelNode);
        return labelVar;
    }

    public void parseCond(ParseTree cond, String label, boolean opposite) {
        Element target = new Label(label);

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
                Element left = parseExpr(cond.getChild(0).getText());
                Element right = parseExpr(cond.getChild(2).getText());
                IR.Opcode opcode = IR.parseCompOp(cond.getChild(1).getText(), opposite);
                ir.add(new IR.Node(opcode, left, right, target));
        }
    }

    // Returns last relevant variable on IR
    public Element parseExpr(String expr) {
        List<Token> infix = Expression.tokenizeExpr(expr, symbolMaps);
        List<Token> postfix = Expression.transformToPostfix(infix);
        Expression.Node tree = Expression.generateExpressionTree(postfix);

        // Expression is a single constant
        if (tree.getToken().isVar())
            return resolveENode(tree);

        // Expression includes an operator
        tree.postorder().forEach(n -> {
            if (n.getToken().isFunction())
                resolveFunction(n);

            if (n.getToken().isOperator()) {
                Operator operator = (Operator)n.getToken();
                operator.setRegister(register++);

                Element op1 = resolveENode(n.get(0));
                Element op2 = resolveENode(n.get(1));
                Element.Type exprType = op1.isFloat() || op2.isFloat() ? Element.Type.FLOAT : Element.Type.INT;
                Element result = new Temporary(operator.getRegister(), exprType);
                ir.add(new IR.Node(IR.parseCalcOp(operator.getValue(), exprType), op1, op2, result));
            }
        });

        return ir.get(ir.size() - 1).getFocus();
    }

    private Element resolveENode(Expression.Node node) {
        if (node.getToken().isFunction())
            return resolveFunction(node);

        Element el = node.getToken().toElement(symbolMaps, scope);
        if (el == null)
            throw new MicroRuntimeException(MicroErrorMessages.UndefinedVariable);

        if (el.isConstant()) {
            IR.Opcode opcode = el.isInt() ? IR.Opcode.STOREI : IR.Opcode.STOREF;
            Element temp = new Temporary(register++, el.getType());
            ir.add(new IR.Node(opcode, el, temp));
            return temp;
        }

        return el;
    }

    private Element resolveFunction(Expression.Node node) {
        ir.add(new IR.Node(IR.Opcode.PUSH));
        node.forEach(p -> ir.add(new IR.Node(IR.Opcode.PUSH, resolveENode(p))));
        ir.add(new IR.Node(IR.Opcode.JSR, new Label(node.getToken().getValue())));
        node.forEach(p -> ir.add(new IR.Node(IR.Opcode.POP)));
        ir.add(new IR.Node(IR.Opcode.POP, new Temporary(register++)));
        return ir.get(ir.size() - 1).getFocus();
    }

}
