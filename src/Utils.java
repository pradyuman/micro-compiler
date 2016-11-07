import java.util.LinkedList;
import java.util.List;

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

        public String getValue() {
            return value;
        }

        public Type getType() {
            return type;
        }
    }

    public static List<Token> tokenizeExpr(String expr) {
        List<Token> list = new LinkedList<>();
        for (String s : expr.split("((?<=[+=*/()])|(?=[+=*/()]))")) {
            Token.Type t = Token.Type.VAR;
            if (s.matches("[+=*/]")) {
                t = Token.Type.OPERATOR;
            } else if (s.equals("(")) {
                t = Token.Type.LPAREN;
            } else if (s.equals(")")) {
                t = Token.Type.RPAREN;
            }
            list.add(new Token(t, s));
        }

        return list;
    }

}
