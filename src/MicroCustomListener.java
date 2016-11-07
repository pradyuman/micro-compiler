import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class MicroCustomListener extends MicroBaseListener {

    private List<SymbolTable> symbolTables;
    private int blocknum;

    public MicroCustomListener() {
        this.symbolTables = new ArrayList<SymbolTable>();
        this.blocknum = 1;
    }

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        symbolTables.add(new SymbolTable("GLOBAL"));
    }

    @Override
    public void exitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        for (SymbolTable s : symbolTables) {
            System.out.println(s);
        }
    }

    @Override
    public void enterString_decl(MicroParser.String_declContext ctx) {
        String name = ctx.getChild(1).getText();
        symbolTables
            .get(symbolTables.size()-1)
            .put(name, new SymbolTable.Variable(name,
                    SymbolTable.Variable.Type.STRING,
                    ctx.getChild(3).getText()));
    }

    @Override
    public void enterVar_decl(MicroParser.Var_declContext ctx) {
        String rawtype = ctx.getChild(0).getText();
        SymbolTable.Variable.Type type = SymbolTable.Variable.Type.valueOf(rawtype);

        for (String s : ctx.getChild(1).getText().split(",")) {
            symbolTables
                .get(symbolTables.size()-1)
                .put(s, new SymbolTable.Variable(s, type));
        }
    }

    @Override
    public void enterParam_decl(MicroParser.Param_declContext ctx) {
        String name = ctx.getChild(1).getText();
        String rawtype = ctx.getChild(0).getText();
        SymbolTable.Variable.Type type = SymbolTable.Variable.Type.valueOf(rawtype);

        symbolTables
            .get(symbolTables.size()-1)
            .put(name, new SymbolTable.Variable(name, type));
    }

    @Override
    public void enterFunc_decl(MicroParser.Func_declContext ctx) {
        symbolTables.add(new SymbolTable(ctx.getChild(2).getText()));
    }

    @Override
    public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolTables.add(new SymbolTable("BLOCK " + blocknum));
        blocknum++;
    }

    @Override
    public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
        symbolTables.remove(symbolTables.size()-1);
        blocknum--;
    }

    @Override
    public void enterElse_part(MicroParser.Else_partContext ctx) {
        symbolTables.add(new SymbolTable("BLOCK " + blocknum));
        blocknum++;
    }

    @Override
    public void enterDo_while_stmt(MicroParser.Do_while_stmtContext ctx) {
        symbolTables.add(new SymbolTable("BLOCK " + blocknum));
        blocknum++;
    }

}
