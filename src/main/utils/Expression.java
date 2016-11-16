package main.utils;

import main.MicroErrorMessages;
import main.MicroException;
import main.SymbolMap;

import java.util.*;
import java.util.stream.IntStream;

public final class Expression {

    private Expression() {}

    public static class Token {

        public enum Type {
            VAR, FUNCTION, FSEPARATOR, OPERATOR, LPAREN, RPAREN,
        }

        public static String calcop = "[-+*/(),]";

        private Type type;
        private String value;
        private int numParam;

        public Token(Type type, String value) {
            this.type = type;
            this.value = value;
        }

        public Token(Type type, String value, int numParam) {
            this.type = type;
            this.value = value;
            this.numParam = numParam;
        }

        @Override
        public boolean equals(Object o) {
            Token t = (Token)o;
            return type == t.getType() && value.equals(t.getValue());
        }

        @Override
        public int hashCode() {
            return type.hashCode() + value.hashCode();
        }

        @Override
        public String toString() {
            return value;
        }

        public int getNumParam() {
            return numParam;
        }

        public String getValue() {
            return value;
        }

        public Type getType() {
            return type;
        }

        public boolean isFunction() {
            return type == Type.FUNCTION;
        }

        public boolean isLParen() {
            return type == Type.LPAREN;
        }

        public boolean isRParen() {
            return type == Type.RPAREN;
        }

        public boolean isOperator() {
            return type == Type.OPERATOR;
        }

    }

    public static final class Operator extends Token {

        private int precedence;
        private int register;

        public Operator(String value) {
            super(Type.OPERATOR, value);
            if (value.matches("[<]")) this.precedence = 0;
            else this.precedence = value.matches("[+-]") ? 1 : 2;
        }

        public int getPrecedence() {
            return precedence;
        }

        public int getRegister() {
            return register;
        }

        public void setRegister(int register) {
            this.register = register;
        }

        public boolean isHigherPrecedence(Operator t) {
            return precedence > t.getPrecedence();
        }
    }

    // Binary Expression Node
    public static final class BENode extends LinkedList<BENode> {

        private Token token;
        private List<BENode> postorder;

        public BENode(Token token) {
            super();
            this.token = token;
            add(null);
            add(null);
        }

        public Token getToken() {
            return token;
        }

        public BENode getLeft() {
            return get(0);
        }

        public void setLeft(BENode node) {
            set(0, node);
        }

        public BENode getRight() {
            return get(1);
        }

        public void setRight(BENode node) {
            set(1, node);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            forEach(n -> b.append(n.getToken() + " "));
            return b.toString();
        }

        @Override
        public void addFirst(BENode n) {
            if (stream().allMatch(e -> e == null))
                clear();

            super.addFirst(n);
        }

        @Override
        public Iterator<BENode> iterator() {
            this.postorder = new LinkedList<>();

            BENode cur = BENode.this;
            Stack<BENode> stack = new Stack<>();

            while (true) {
                while (cur != null) {
                    if (cur.getRight() != null) {
                        stack.push(cur.getRight());
                    }
                    stack.push(cur);
                    cur = cur.getLeft();
                }

                if (stack.empty()) break;

                cur = stack.pop();
                if (!stack.empty() && cur.getRight() != null && cur.getRight() == stack.peek()) {
                    stack.pop();
                    stack.push(cur);
                    cur = cur.getRight();
                } else {
                    this.postorder.add(cur);
                    cur = null;
                }
            }

            return postorder.iterator();
        }

    }

    public static List<Token> tokenizeExpr(String expr, List<SymbolMap> symbolMaps) {
        List<Token> list = new LinkedList<>();

        for (String s : expr.split("(?<=op)|(?=op)".replace("op", Token.calcop))) {
            Token.Type t = Token.Type.VAR;
            SymbolMap func = symbolMaps.stream().filter(m -> m.getName().equals(s)).findFirst().orElse(null);

            if (s.equals("(")) {
                t = Token.Type.LPAREN;
            } else if (s.equals(")")) {
                t = Token.Type.RPAREN;
            } else if (s.equals(",")) {
                t = Token.Type.FSEPARATOR;
            } else if (s.matches(Token.calcop)) {
                t = Token.Type.OPERATOR;
            } else if (func != null) {
                t = Token.Type.FUNCTION;
            }

            switch (t) {
                case FUNCTION:
                    list.add(new Token(t, s, func.getNumParam())); break;
                case OPERATOR:
                    list.add(new Operator(s)); break;
                default:
                    list.add(new Token(t, s));
            }
        }

        return list;
    }

    public static List<Token> transformToPostfix(List<Token> infix) {
        List<Token> postfix = new LinkedList<>();
        Deque<Token> stack = new ArrayDeque<>();

        infix.forEach(t -> {
            Token top = stack.peek();
            switch (t.getType()) {
                case VAR:
                    postfix.add(t);
                    break;
                case FUNCTION:
                    stack.push(t);
                    break;
                case FSEPARATOR:
                    while (top != null && top.isOperator() && !top.isLParen()) {
                        postfix.add(stack.pop());
                        top = stack.peek();
                    }

                    if (!top.isLParen())
                        throw new MicroException(MicroErrorMessages.MismatchedParentheses);

                    break;
                case OPERATOR:
                    while (top != null && top.isOperator() && !((Operator)t).isHigherPrecedence((Operator)top)) {
                        postfix.add(stack.pop());
                        top = stack.peek();
                    }
                    stack.push(t);
                    break;
                case LPAREN:
                    stack.push(t);
                    break;
                case RPAREN:
                    while(stack.peek() != null && !stack.peek().isLParen())
                        postfix.add(stack.pop());

                    if (!stack.peek().isLParen())
                        throw new MicroException(MicroErrorMessages.MismatchedParentheses);

                    // Pop LParen
                    stack.pop();

                    if (stack.peek().isFunction())
                        postfix.add(stack.pop());

                    break;
            }
        });

        while (stack.peek() != null) {
            if (stack.peek().isLParen() || stack.peek().isRParen())
                throw new MicroException(MicroErrorMessages.MismatchedParentheses);

            postfix.add(stack.pop());
        }

        return postfix;
    }

    public static BENode generateExpressionTree(List<Token> postfix) {
        Stack<BENode> stack = new Stack<>();
        postfix.forEach(t -> {
           if (t.isOperator()) {
               BENode n = new BENode(t);
               n.setRight(stack.pop());
               n.setLeft(stack.pop());
               stack.push(n);
           } else if (t.isFunction()) {
               BENode n = new BENode(t);
               IntStream.range(0, t.getNumParam())
                       .forEach(__ -> n.addFirst(stack.pop()));
               stack.push(n);
            } else {
               stack.push(new BENode(t));
           }
        });
        return stack.pop();
    }

}
