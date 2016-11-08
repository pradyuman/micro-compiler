import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Utils {

    private Utils() {}

    public static class Token {

        public enum Type {
            VAR, OPERATOR, LPAREN, RPAREN,
        }

        private Type type;
        private String value;

        public Token(Type type, String value) {
            this.type = type;
            this.value = value;
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
            return String.format("TYPE: %s | VALUE: %s", type, value);
        }

        public String getValue() {
            return value;
        }

        public Type getType() {
            return type;
        }

        public boolean isOperator() {
            return type == Type.OPERATOR;
        }

        public boolean isLParen() {
            return type == Type.LPAREN;
        }
    }

    public static class Operator extends Token {

        private int precedence;

        public Operator(Type type, String value) {
            super(type, value);
            this.precedence = value.matches("[+-]") ? 1 : 2;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isHigherPrecedence(Operator t) {
            return precedence > t.getPrecedence();
        }
    }

    public static List<Token> tokenizeExpr(String expr) {
        List<Token> list = new LinkedList<>();
        for (String s : expr.split("((?<=[+\\-*/()])|(?=[+\\-*/()]))")) {
            Token.Type t = Token.Type.VAR;
            if (s.matches("[+\\-*/]")) {
                t = Token.Type.OPERATOR;
            } else if (s.equals("(")) {
                t = Token.Type.LPAREN;
            } else if (s.equals(")")) {
                t = Token.Type.RPAREN;
            }

            if (t == Token.Type.OPERATOR) {
                list.add(new Operator(t, s));
            } else {
                list.add(new Token(t, s));
            }
        }

        return list;
    }

    public static List<Token> transformToPostfix(List<Token> infix) {
        List<Token> postfix = new LinkedList<>();
        Stack<Token> stack = new Stack<>();

        for (Token t : infix) {
            switch (t.getType()) {
                case VAR:
                    postfix.add(t);
                    break;
                case OPERATOR:
                    Token top = stack.empty() ? null : stack.peek();
                    while (!stack.empty() && top.isOperator() && !((Operator)t).isHigherPrecedence((Operator)top)) {
                        postfix.add(stack.pop());
                        top = stack.empty() ? null : stack.peek();
                    }
                    stack.push(t);
                    break;
                case LPAREN:
                    stack.push(t);
                    break;
                case RPAREN:
                    // (TODO) Better error handling: if stack runs out without a LPAREN then mismatched parentheses
                    while(!stack.empty() && !stack.peek().isLParen()) {
                        postfix.add(stack.pop());
                    }
                    stack.pop();
                    break;
            }
        }

        // (TODO) Better error handling: if top of stack is parenthesis then mismatched parentheses
        while (!stack.empty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

}
