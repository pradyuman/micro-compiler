import java.util.*;

public final class ExpressionUtils {

    private ExpressionUtils() {}

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
            return value;
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

    public static final class Operator extends Token {

        private int precedence;
        private int register;

        public Operator(String value) {
            super(Type.OPERATOR, value);
            if (value.equals(":=")) this.precedence = 0;
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
    public static final class BENode implements Iterable<BENode> {

        private Token token;
        private BENode left;
        private BENode right;
        private List<BENode> postorder;

        public BENode(Token token) {
            super();
            this.token = token;
        }

        public Token getToken() {
            return token;
        }

        public BENode getLeft() {
            return left;
        }

        public void setLeft(BENode node) {
            this.left = node;
        }

        public BENode getRight() {
            return right;
        }

        public void setRight(BENode node) {
            this.right = node;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            forEach(n -> b.append(n.getToken() + " "));
            return b.toString();
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
                list.add(new Operator(s));
            } else {
                list.add(new Token(t, s));
            }
        }

        return list;
    }

    public static List<Token> transformToPostfix(List<Token> infix) {
        List<Token> postfix = new LinkedList<>();
        Stack<Token> stack = new Stack<>();

        infix.forEach(t -> {
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
        });

        // (TODO) Better error handling: if top of stack is parenthesis then mismatched parentheses
        while (!stack.empty()) {
            postfix.add(stack.pop());
        }

        return postfix;
    }

    public static BENode generateExpressionTree(List<Token> postfix) {
        Stack<BENode> stack = new Stack<>();
        postfix.forEach(t -> {
           if (!t.isOperator()) {
               stack.push(new BENode(t));
           } else {
               BENode n = new BENode(t);
               n.setRight(stack.pop());
               n.setLeft(stack.pop());
               stack.push(n);
           }
        });
        return stack.pop();
    }

}
