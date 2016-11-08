package main.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExpressionTest {

    private List<Expression.Token> infixOnlyConstants;
    private List<Expression.Token> infixWithVariables;

    @Before
    public void initialize() {
        // Initialize onlyConstants;
        infixOnlyConstants = new LinkedList<>();
        infixOnlyConstants.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Operator("+"),
                new Expression.Token(Expression.Token.Type.VAR, "4"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Operator("/"),
                new Expression.Token(Expression.Token.Type.LPAREN, "("),
                new Expression.Token(Expression.Token.Type.VAR, "1"),
                new Expression.Operator("-"),
                new Expression.Token(Expression.Token.Type.VAR, "5"),
                new Expression.Token(Expression.Token.Type.RPAREN, ")")
        ));

        // Initialize withVariables;
        infixWithVariables = new LinkedList<>();
        infixWithVariables.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.VAR, "test"),
                new Expression.Operator("+"),
                new Expression.Token(Expression.Token.Type.VAR, "a"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Operator("/"),
                new Expression.Token(Expression.Token.Type.LPAREN, "("),
                new Expression.Token(Expression.Token.Type.VAR, "1"),
                new Expression.Operator("-"),
                new Expression.Token(Expression.Token.Type.VAR, "5"),
                new Expression.Token(Expression.Token.Type.RPAREN, ")")
        ));
    }

    @Test
    public void tokenizeExprOnlyConstants() throws Exception {
        List<Expression.Token> actual = Expression.tokenizeExpr("3+4*2/(1-5)");
        assertEquals(infixOnlyConstants, actual);
    }

    @Test
    public void tokenizeExprWithVariables() throws Exception {
        List<Expression.Token> actual = Expression.tokenizeExpr("test+a*2/(1-5)");
        assertEquals(infixWithVariables, actual);
    }

    @Test
    public void transformToPostfixOnlyConstants() throws Exception {
        List<Expression.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Token(Expression.Token.Type.VAR, "4"),
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.VAR, "1"),
                new Expression.Token(Expression.Token.Type.VAR, "5"),
                new Expression.Operator("-"),
                new Expression.Operator("/"),
                new Expression.Operator("+")
        ));
        List<Expression.Token> actual = Expression.transformToPostfix(infixOnlyConstants);
        assertEquals(expected, actual);
    }

    @Test
    public void transformToPostfixWithVariables() throws Exception {
        List<Expression.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.VAR, "test"),
                new Expression.Token(Expression.Token.Type.VAR, "a"),
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.VAR, "1"),
                new Expression.Token(Expression.Token.Type.VAR, "5"),
                new Expression.Operator("-"),
                new Expression.Operator("/"),
                new Expression.Operator("+")
        ));
        List<Expression.Token> actual = Expression.transformToPostfix(infixWithVariables);
        assertEquals(expected, actual);
    }

}