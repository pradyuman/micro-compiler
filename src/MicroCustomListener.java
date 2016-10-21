import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class MicroCustomListener extends MicroBaseListener {

    private List<SymbolTable> symbolTables;

    public MicroCustomListener() {
        this.symbolTables = new ArrayList<SymbolTable>();
    }

    @Override
    public void enterPgm_body(MicroParser.Pgm_bodyContext ctx) {
        System.out.println(ctx.getText());
    }

}
