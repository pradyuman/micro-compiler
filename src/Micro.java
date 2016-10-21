import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro {

    public static class MicroFailFastLexer extends MicroLexer {

        public MicroFailFastLexer(CharStream input) { super(input); }

        public void recover(LexerNoViableAltException e) {
            throw new MicroException(e);
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
            MicroCustomListener listener = new MicroCustomListener();
            walker.walk(listener, microProgramContext);
        } catch (MicroException e) {
            System.out.println("Not Accepted");
        }
    }

}
