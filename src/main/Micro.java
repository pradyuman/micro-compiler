package main;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro {

    public static class MicroFailFastLexer extends MicroLexer {

        public MicroFailFastLexer(CharStream input) { super(input); }

        public void recover(LexerNoViableAltException e) {
            throw new MicroRuntimeException(e);
        }

    }

    public static void main(String[] args) throws Exception {
        ANTLRFileStream input = new ANTLRFileStream(args[0]);

        try {
            MicroFailFastLexer lexer = new MicroFailFastLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MicroParser parser = new MicroParser(tokens);
            parser.setErrorHandler(new MicroErrorStrategy());

            MicroParser.ProgramContext microProgramContext = parser.program();
            ParseTreeWalker walker = new ParseTreeWalker();
            MicroCompiler compiler = new MicroCompiler();
            walker.walk(compiler, microProgramContext);
        } catch (MicroRuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
