package compiler.expression;

import compiler.IR;
import compiler.element.Element;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import compiler.MicroErrorMessages;
import compiler.MicroRuntimeException;
import compiler.SymbolMap;

public final class Expression {

    private Expression() {}

    /**
     * expression Node:
     *  - [-+/*] are binary
     *  - functions can have any number of children
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static final class Node extends LinkedList<Node> {

        private Token token;

        public Node(Token token) {
            super();
            this.token = token;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            forEach(n -> b.append(n.getToken() + " "));
            return b.toString();
        }

        /*
        public Element toElement(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, IR ir, Integer register) {
            if (token.isFunction()) {
                IR funcIR = token.toIR(symbolMaps, scope, this, register);
                ir.addAll(funcIR);
                return funcIR.get(funcIR.size() - 1).getFocus();
            }

            Element el
        }
        */

        public IR toIR(List<SymbolMap> symbolMaps, LinkedList<Integer> scope, Integer register) {
            return postorder().stream()
                    .flatMap(n -> n.getToken().toIR(symbolMaps, scope, n, register).stream())
                    .collect(Collectors.toCollection(() -> new IR(symbolMaps.get(0))));
        }


        public List<Node> postorder() {
            List<Node>postorder = new LinkedList<>();

            @Data
            @AllArgsConstructor
            class NodeState {
                private Node node;
                private boolean visited;
            }

            NodeState state = new NodeState(Node.this, false);
            Deque<NodeState> stack = new LinkedList<>();
            stack.push(state);

            while (true) {
                if (stack.size() == 0) break;

                if (stack.peek().isVisited()) {
                    postorder.add(stack.pop().getNode());
                    continue;
                }

                state = stack.peek();
                state.setVisited(true);
                Node cur = state.getNode();

                for (int i = cur.size() - 1; i >= 0; i--) {
                    stack.push(new NodeState(cur.get(i), cur.get(i).size() == 0));
                }
            }

            return postorder;
        }

    }

    public static List<Token> tokenizeExpr(String expr, List<SymbolMap> symbolMaps) {
        List<Token> list = new LinkedList<>();

        for (String s : expr.split("(?<=op)|(?=op)".replace("op", compiler.expression.Token.calcop))) {
            Token.Type t = Token.Type.VAR;
            SymbolMap func = symbolMaps.stream().filter(m -> m.getName().equals(s)).findFirst().orElse(null);

            if (s.equals("(")) {
                t = Token.Type.LPAREN;
            } else if (s.equals(")")) {
                t = Token.Type.RPAREN;
            } else if (s.equals(",")) {
                t = Token.Type.FSEPARATOR;
            } else if (s.matches(compiler.expression.Token.calcop)) {
                t = Token.Type.OPERATOR;
            } else if (func != null) {
                t = Token.Type.FUNCTION;
            }

            switch (t) {
                case FUNCTION:
                    list.add(new Function(s, func.getNumParam())); break;
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

    public static Node generateExpressionTree(List<Token> postfix) {
        Deque<Node> stack = new ArrayDeque<>();
        postfix.forEach(t -> {
           if (t.isOperator()) {
               Node n = new Node(t);
               n.addFirst(stack.pop());
               n.addFirst(stack.pop());
               stack.push(n);
           } else if (t.isFunction()) {
               Node n = new Node(t);
               IntStream.range(0, ((Function)t).getNumParam())
                       .forEach(__ -> n.addFirst(stack.pop()));
               stack.push(n);
            } else {
               stack.push(new Node(t));
           }
        });
        return stack.pop();
    }

}
