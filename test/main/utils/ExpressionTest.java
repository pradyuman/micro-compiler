package main.utils;

import main.SymbolMap;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ExpressionTest {

    private List<Expression.Token> infixOnlyConstants;
    private List<Expression.Token> infixWithVariables;
    private List<Expression.Token> infixWithFunctions;
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

        // Initialize withFunctions
        infixWithFunctions = new LinkedList<>();
        infixWithFunctions.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.FUNCTION, "sin"),
                new Expression.Token(Expression.Token.Type.LPAREN, "("),
                new Expression.Token(Expression.Token.Type.FUNCTION, "max"),
                new Expression.Token(Expression.Token.Type.LPAREN, "("),
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Token(Expression.Token.Type.FSEPARATOR, ","),
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Token(Expression.Token.Type.RPAREN, ")"),
                new Expression.Operator("/"),
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.VAR, "3.1415"),
                new Expression.Token(Expression.Token.Type.RPAREN, ")")

        ));

    }

    @Test
    public void tokenizeExprOnlyConstants() throws Exception {
        List<Expression.Token> actual = Expression.tokenizeExpr("3+4*2/(1-5)", symbolMaps);
        assertEquals(infixOnlyConstants, actual);
    }

    @Test
    public void tokenizeExprWithVariables() throws Exception {
        List<Expression.Token> actual = Expression.tokenizeExpr("test+a*2/(1-5)", symbolMaps);
        assertEquals(infixWithVariables, actual);
    }

    @Test
    public void tokenizeExprWithFunctions() throws Exception {
        List<Expression.Token> actual = Expression.tokenizeExpr("sin(max(2,3)/3*3.1415)", symbolMaps);
        assertEquals(infixWithFunctions, actual);
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

    @Test
    public void transformToPostfixWithFunctions() throws Exception {
        List<Expression.Token> expected = new LinkedList<>();
        expected.addAll(Arrays.asList(
                new Expression.Token(Expression.Token.Type.VAR, "2"),
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Token(Expression.Token.Type.FUNCTION, "max"),
                new Expression.Token(Expression.Token.Type.VAR, "3"),
                new Expression.Operator("/"),
                new Expression.Token(Expression.Token.Type.VAR, "3.1415"),
                new Expression.Operator("*"),
                new Expression.Token(Expression.Token.Type.FUNCTION, "sin")
        ));
        List<Expression.Token> actual = Expression.transformToPostfix(infixWithFunctions);
        assertEquals(expected, actual);
    }

}