import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExpressionUtilsTest {

    private List<ExpressionUtils.Token> infixOnlyConstants;
    private List<ExpressionUtils.Token> infixWithVariables;

    @Before
    public void initialize() {
        // Initialize onlyConstants;
        infixOnlyConstants = new LinkedList<>();
        infixOnlyConstants.addAll(Arrays.asList(
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "3"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "+"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "4"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "*"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "2"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "/"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.LPAREN, "("),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "1"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "-"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "5"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.RPAREN, ")")
        ));

        // Initialize withVariables;
        infixWithVariables = new LinkedList<>();
        infixWithVariables.addAll(Arrays.asList(
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "test"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "+"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "a"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "*"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "2"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "/"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.LPAREN, "("),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "1"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "-"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "5"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.RPAREN, ")")
        ));
    }

    @Test
    public void tokenizeExprOnlyConstants() throws Exception {
        List<ExpressionUtils.Token> actual = ExpressionUtils.tokenizeExpr("3+4*2/(1-5)");
        assertEquals(infixOnlyConstants, actual);
    }

    @Test
    public void tokenizeExprWithVariables() throws Exception {
        List<ExpressionUtils.Token> actual = ExpressionUtils.tokenizeExpr("test+a*2/(1-5)");
        assertEquals(infixWithVariables, actual);
    }

    @Test
    public void transformToPostfixOnlyConstants() throws Exception {
        List<ExpressionUtils.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "3"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "4"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "2"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "*"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "1"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "5"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "-"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "/"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "+")
        ));
        List<ExpressionUtils.Token> actual = ExpressionUtils.transformToPostfix(infixOnlyConstants);
        assertEquals(expected, actual);
    }

    @Test
    public void transformToPostfixWithVariables() throws Exception {
        List<ExpressionUtils.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "test"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "a"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "2"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "*"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "1"),
                new ExpressionUtils.Token(ExpressionUtils.Token.Type.VAR, "5"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "-"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "/"),
                new ExpressionUtils.Operator(ExpressionUtils.Token.Type.OPERATOR, "+")
        ));
        List<ExpressionUtils.Token> actual = ExpressionUtils.transformToPostfix(infixWithVariables);
        assertEquals(expected, actual);
    }

}