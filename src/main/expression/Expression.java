package main.expression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import main.MicroErrorMessages;
import main.MicroRuntimeException;
import main.SymbolMap;

public final class Expression {

    private Expression() {}

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static final class Operator extends Token {

        private int precedence;
        private int register;

        public Operator(String value) {
            super(Type.OPERATOR, value);
            if (value.matches("[<]")) this.precedence = 0;
            else this.precedence = value.matches("[+-]") ? 1 : 2;
        }

        public boolean isHigherPrecedence(Operator t) {
            return precedence > t.getPrecedence();
        }
    }

    /**
     * expression Node:
     *  - [-+/*] are binary
     *  - functions can have any number of children
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class ENode extends LinkedList<ENode> {

        private Token token;

        public ENode(Token token) {
            super();
            this.token = token;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            forEach(n -> b.append(n.getToken() + " "));
            return b.toString();
        }

        public List<ENode> postorder() {
            List<ENode>postorder = new LinkedList<>();

            @Data
            @AllArgsConstructor
            class ENodeState {
                private ENode node;
                private boolean visited;
            }

            ENodeState state = new ENodeState(ENode.this, false);
            Deque<ENodeState> stack = new LinkedList<>();
            stack.push(state);

            while (true) {
                if (stack.size() == 0) break;

                if (stack.peek().isVisited()) {
                    postorder.add(stack.pop().getNode());
                    continue;
                }

                state = stack.peek();
                state.setVisited(true);
                ENode cur = state.getNode();

                for (int i = cur.size() - 1; i >= 0; i--) {
                    stack.push(new ENodeState(cur.get(i), cur.get(i).size() == 0));
                }
            }

            return postorder;
        }

    }

    public static List<Token> tokenizeExpr(String expr, List<SymbolMap> symbolMaps) {
        List<Token> list = new LinkedList<>();

        for (String s : expr.split("(?<=op)|(?=op)".replace("op", main.expression.Token.calcop))) {
            Token.Type t = main.expression.Token.Type.VAR;
            SymbolMap func = symbolMaps.stream().filter(m -> m.getName().equals(s)).findFirst().orElse(null);

            if (s.equals("(")) {
                t = main.expression.Token.Type.LPAREN;
            } else if (s.equals(")")) {
                t = main.expression.Token.Type.RPAREN;
            } else if (s.equals(",")) {
                t = main.expression.Token.Type.FSEPARATOR;
            } else if (s.matches(main.expression.Token.calcop)) {
                t = main.expression.Token.Type.OPERATOR;
            } else if (func != null) {
                t = main.expression.Token.Type.FUNCTION;
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
                        throw new MicroRuntimeException(MicroErrorMessages.MismatchedParentheses);

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
                        throw new MicroRuntimeException(MicroErrorMessages.MismatchedParentheses);

                    // Pop LParen
                    stack.pop();

                    if (stack.size() != 0 && stack.peek().isFunction())
                        postfix.add(stack.pop());

                    break;
            }
        });

        while (stack.peek() != null) {
            if (stack.peek().isLParen() || stack.peek().isRParen())
                throw new MicroRuntimeException(MicroErrorMessages.MismatchedParentheses);

            postfix.add(stack.pop());
        }

        return postfix;
    }

    public static ENode generateExpressionTree(List<Token> postfix) {
        Deque<ENode> stack = new ArrayDeque<>();
        postfix.forEach(t -> {
           if (t.isOperator()) {
               ENode n = new ENode(t);
               n.addFirst(stack.pop());
               n.addFirst(stack.pop());
               stack.push(n);
           } else if (t.isFunction()) {
               ENode n = new ENode(t);
               IntStream.range(0, t.getNumParam())
                       .forEach(__ -> n.addFirst(stack.pop()));
               stack.push(n);
            } else {
               stack.push(new ENode(t));
           }
        });
        return stack.pop();
    }

}
