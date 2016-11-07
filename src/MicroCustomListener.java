import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MicroCustomListener extends MicroBaseListener {

    private int blocknum;
    private List<IR.Node> ir;
    private Stack<Integer> scope;
    private List<SymbolMap> symbolMaps;

    public MicroCustomListener() {
        this.blocknum = 1;
        this.ir = new IR();
        this.scope = new Stack<>();
        this.symbolMaps = new ArrayList<>();
    }

    private SymbolMap lastMap() {
        return symbolMaps.get(symbolMaps.size()-1);
    }

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        scope.push(0);
        symbolMaps.add(new SymbolMap("GLOBAL"));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        /*
        for (SymbolMap m : symbolMaps) {
            System.out.println(m);
        }
        */
        for (IR.Node n : ir) {
            System.out.println(";" + n);
        }
    }

    @Override
    public void enterString_decl(MicroParser.String_declContext ctx) {
        String name = ctx.getChild(1).getText();
        Variable<String> var = new Variable<>(name, Variable.Type.STRING, ctx.getChild(3).getText());
        symbolMaps.get(symbolMaps.size()-1).put(name, var);
    }

    @Override
    public void enterVar_decl(MicroParser.Var_declContext ctx) {
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);

        for (String s : ctx.getChild(1).getText().split(",")) {
            lastMap().put(s, new Variable(s, type));
        }
    }

    @Override
    public void enterParam_decl(MicroParser.Param_declContext ctx) {
        String name = ctx.getChild(1).getText();
        String rawtype = ctx.getChild(0).getText();
        Variable.Type type = Variable.Type.valueOf(rawtype);
        lastMap().put(name, new Variable(name, type));
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
        String id = ctx.getChild(0).getText();
        Variable var = symbolMaps.get(scope.peek()).get(id);
        for (int i = scope.size()-2; i >= 0 && var == null; i--) {
            var = symbolMaps.get(scope.get(i)).get(id);
        }
        // (TODO) Do some better error checking for var == null (throw exception)
        if (var == null) return;
        System.out.println(var);
        System.out.println(ctx.getChild(2).getText());
        for (Utils.Token t : Utils.tokenizeExpr(ctx.getChild(2).getText())) {
            System.out.print(t.getValue() + " ");
        }
        System.out.println();
    }

    @Override
    public void enterRead_stmt(MicroParser.Read_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = symbolMaps.get(scope.peek()).get(s);
            for (int i = scope.size()-2; i >= 0 && var == null; i--) {
                var = symbolMaps.get(scope.get(i)).get(s);
            }
            // (TODO) Do some better error checking for var == null (throw exception)
            if (var == null) return;
            IR.Opcode opcode = var.isInt() ? IR.Opcode.READI : IR.Opcode.READF;
            ir.add(new IR.Node(opcode, var));
        }
    }

    @Override
    public void enterWrite_stmt(MicroParser.Write_stmtContext ctx) {
        for (String s : ctx.getChild(2).getText().split(",")) {
            Variable var = symbolMaps.get(scope.peek()).get(s);
            for (int i = scope.size()-2; i >= 0 && var == null; i--) {
                var = symbolMaps.get(scope.get(i)).get(s);
            }
            // (TODO) Do some error checking for var == null (throw exception)
            if (var == null) return;
            IR.Opcode opcode = var.isInt() ? IR.Opcode.WRITEI : IR.Opcode.WRITEF;
            ir.add(new IR.Node(opcode, var));
        }
    }

}
