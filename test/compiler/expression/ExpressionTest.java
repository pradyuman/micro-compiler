package compiler.expression;

import compiler.SymbolMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ExpressionTest {

    private List<Token> infixOnlyConstants;
    private List<Token> infixWithVariables;
    private List<Token> infixWithFunctions;
    private List<SymbolMap> symbolMaps;

    @Before
    public void initialize() {
        symbolMaps = new ArrayList<>();
        symbolMaps.addAll(Arrays.asList(
                new SymbolMap("sin"),
                new SymbolMap("max")
        ));

        // Initialize onlyConstants;
        infixOnlyConstants = new LinkedList<>();
        infixOnlyConstants.addAll(Arrays.asList(
                new Token(Token.Type.VAR, "3"),
                new Operator("+"),
                new Token(Token.Type.VAR, "4"),
                new Operator("*"),
                new Token(Token.Type.VAR, "2"),
                new Operator("/"),
                new Token(Token.Type.LPAREN, "("),
                new Token(Token.Type.VAR, "1"),
                new Operator("-"),
                new Token(Token.Type.VAR, "5"),
                new Token(Token.Type.RPAREN, ")")
        ));

        // Initialize withVariables;
        infixWithVariables = new LinkedList<>();
        infixWithVariables.addAll(Arrays.asList(
                new Token(Token.Type.VAR, "test"),
                new Operator("+"),
                new Token(Token.Type.VAR, "a"),
                new Operator("*"),
                new Token(Token.Type.VAR, "2"),
                new Operator("/"),
                new Token(Token.Type.LPAREN, "("),
                new Token(Token.Type.VAR, "1"),
                new Operator("-"),
                new Token(Token.Type.VAR, "5"),
                new Token(Token.Type.RPAREN, ")")
        ));

        // Initialize withFunctions
        infixWithFunctions = new LinkedList<>();
        infixWithFunctions.addAll(Arrays.asList(
                new Token(Token.Type.FUNCTION, "sin"),
                new Token(Token.Type.LPAREN, "("),
                new Token(Token.Type.FUNCTION, "max"),
                new Token(Token.Type.LPAREN, "("),
                new Token(Token.Type.VAR, "2"),
                new Token(Token.Type.FSEPARATOR, ","),
                new Token(Token.Type.VAR, "3"),
                new Token(Token.Type.RPAREN, ")"),
                new Operator("/"),
                new Token(Token.Type.VAR, "3"),
                new Operator("*"),
                new Token(Token.Type.VAR, "3.1415"),
                new Token(Token.Type.RPAREN, ")")

        ));

    }

    @Test
    public void tokenizeExprOnlyConstants() throws Exception {
        List<Token> actual = Expression.tokenizeExpr("3+4*2/(1-5)", symbolMaps);
        Assert.assertEquals(infixOnlyConstants, actual);
    }

    @Test
    public void tokenizeExprWithVariables() throws Exception {
        List<Token> actual = Expression.tokenizeExpr("test+a*2/(1-5)", symbolMaps);
        Assert.assertEquals(infixWithVariables, actual);
    }

    @Test
    public void tokenizeExprWithFunctions() throws Exception {
        List<Token> actual = Expression.tokenizeExpr("sin(max(2,3)/3*3.1415)", symbolMaps);
        Assert.assertEquals(infixWithFunctions, actual);
    }

    @Test
    public void transformToPostfixOnlyConstants() throws Exception {
        List<Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Token(Token.Type.VAR, "3"),
                new Token(Token.Type.VAR, "4"),
                new Token(Token.Type.VAR, "2"),
                new Operator("*"),
                new Token(Token.Type.VAR, "1"),
                new Token(Token.Type.VAR, "5"),
                new Operator("-"),
                new Operator("/"),
                new Operator("+")
        ));
        List<Token> actual = Expression.transformToPostfix(infixOnlyConstants);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void transformToPostfixWithVariables() throws Exception {
        List<Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Token(Token.Type.VAR, "test"),
                new Token(Token.Type.VAR, "a"),
                new Token(Token.Type.VAR, "2"),
                new Operator("*"),
                new Token(Token.Type.VAR, "1"),
                new Token(Token.Type.VAR, "5"),
                new Operator("-"),
                new Operator("/"),
                new Operator("+")
        ));
        List<Token> actual = Expression.transformToPostfix(infixWithVariables);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void transformToPostfixWithFunctions() throws Exception {
        List<Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Token(Token.Type.VAR, "2"),
                new Token(Token.Type.VAR, "3"),
                new Token(Token.Type.FUNCTION, "max"),
                new Token(Token.Type.VAR, "3"),
                new Operator("/"),
                new Token(Token.Type.VAR, "3.1415"),
                new Operator("*"),
                new Token(Token.Type.FUNCTION, "sin")
        ));
        List<Token> actual = Expression.transformToPostfix(infixWithFunctions);
        Assert.assertEquals(expected, actual);
    }

}