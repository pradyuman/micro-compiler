import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Micro {
    public static void main(String[] args) throws Exception {
        ANTLRFileStream input = new ANTLRFileStream(args[0]);
        MicroLexer lexer = new MicroLexer(input);

        Vocabulary v = lexer.getVocabulary();
        for (Token token = lexer.nextToken(); token.getType() != Token.EOF;
             token = lexer.nextToken()) {
            System.out.println("Token Type: " + v.getSymbolicName(token.getType()));
            System.out.println("Value: " + token.getText());
        }
    }
}
