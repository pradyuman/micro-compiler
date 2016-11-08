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

        public Operator(String value) {
            super(Type.OPERATOR, value);
            if (value.equals(":=")) this.precedence = 0;
            else this.precedence = value.matches("[+-]") ? 1 : 2;
        }

        public int getPrecedence() {
            return precedence;
        }

        public boolean isHigherPrecedence(Operator t) {
            return precedence > t.getPrecedence();
        }
    }

    public static final class TreeNode implements Iterable<TreeNode> {

        private Token value;
        private TreeNode left;
        private TreeNode right;
        private List<TreeNode> inorder;

        public TreeNode(Token value) {
            super();
            this.value = value;
        }

        public Token getValue() {
            return value;
        }

        public TreeNode getLeft() {
            return left;
        }

        public void setLeft(TreeNode node) {
            this.left = node;
        }

        public TreeNode getRight() {
            return right;
        }

        public void setRight(TreeNode node) {
            this.right = node;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            forEach(n -> b.append(n.getValue() + " "));
            return b.toString();
        }

        @Override
        public Iterator<TreeNode> iterator() {
            this.inorder = new LinkedList<>();

            TreeNode temp;
            TreeNode cur = TreeNode.this;
            Stack<TreeNode> stack = new Stack<>();

            while (true) {
                while (cur != null) {
                    stack.push(cur);
                    cur = cur.getLeft();
                }

                if (stack.empty()) break;

                temp = stack.pop();
                this.inorder.add(temp);
                cur = temp.getRight();
            }
            return inorder.iterator();
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

    public static TreeNode generateExpressionTree(List<Token> postfix) {
        Stack<TreeNode> stack = new Stack<>();
        postfix.forEach(t -> {
           if (!t.isOperator()) {
               stack.push(new TreeNode(t));
           } else {
               TreeNode n = new TreeNode(t);
               n.setRight(stack.pop());
               n.setLeft(stack.pop());
               stack.push(n);
           }
        });
        return stack.pop();
    }

}
