import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    private List<Utils.Token> infixOnlyConstants;
    private List<Utils.Token> infixWithVariables;

    @Before
    public void initialize() {
        // Initialize onlyConstants;
        infixOnlyConstants = new LinkedList<>();
        infixOnlyConstants.addAll(Arrays.asList(
                new Utils.Token(Utils.Token.Type.VAR, "3"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "+"),
                new Utils.Token(Utils.Token.Type.VAR, "4"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "*"),
                new Utils.Token(Utils.Token.Type.VAR, "2"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "/"),
                new Utils.Token(Utils.Token.Type.LPAREN, "("),
                new Utils.Token(Utils.Token.Type.VAR, "1"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "-"),
                new Utils.Token(Utils.Token.Type.VAR, "5"),
                new Utils.Token(Utils.Token.Type.RPAREN, ")")
        ));

        // Initialize withVariables;
        infixWithVariables = new LinkedList<>();
        infixWithVariables.addAll(Arrays.asList(
                new Utils.Token(Utils.Token.Type.VAR, "test"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "+"),
                new Utils.Token(Utils.Token.Type.VAR, "a"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "*"),
                new Utils.Token(Utils.Token.Type.VAR, "2"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "/"),
                new Utils.Token(Utils.Token.Type.LPAREN, "("),
                new Utils.Token(Utils.Token.Type.VAR, "1"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "-"),
                new Utils.Token(Utils.Token.Type.VAR, "5"),
                new Utils.Token(Utils.Token.Type.RPAREN, ")")
        ));
    }

    @Test
    public void tokenizeExprOnlyConstants() throws Exception {
        List<Utils.Token> actual = Utils.tokenizeExpr("3+4*2/(1-5)");
        assertEquals(infixOnlyConstants, actual);
    }

    @Test
    public void tokenizeExprWithVariables() throws Exception {
        List<Utils.Token> actual = Utils.tokenizeExpr("test+a*2/(1-5)");
        assertEquals(infixWithVariables, actual);
    }

    @Test
    public void transformToPostfixOnlyConstants() throws Exception {
        List<Utils.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Utils.Token(Utils.Token.Type.VAR, "3"),
                new Utils.Token(Utils.Token.Type.VAR, "4"),
                new Utils.Token(Utils.Token.Type.VAR, "2"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "*"),
                new Utils.Token(Utils.Token.Type.VAR, "1"),
                new Utils.Token(Utils.Token.Type.VAR, "5"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "-"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "/"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "+")
        ));
        List<Utils.Token> actual = Utils.transformToPostfix(infixOnlyConstants);
        assertEquals(expected, actual);
    }

    @Test
    public void transformToPostfixWithVariables() throws Exception {
        List<Utils.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Utils.Token(Utils.Token.Type.VAR, "test"),
                new Utils.Token(Utils.Token.Type.VAR, "a"),
                new Utils.Token(Utils.Token.Type.VAR, "2"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "*"),
                new Utils.Token(Utils.Token.Type.VAR, "1"),
                new Utils.Token(Utils.Token.Type.VAR, "5"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "-"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "/"),
                new Utils.Operator(Utils.Token.Type.OPERATOR, "+")
        ));
        List<Utils.Token> actual = Utils.transformToPostfix(infixWithVariables);
        assertEquals(expected, actual);
    }

}