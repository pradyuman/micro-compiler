package compiler.expression;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {

    public enum Type {
        VAR, FUNCTION, FSEPARATOR, OPERATOR, LPAREN, RPAREN,
    }

    public static String calcop = "[-+*/(),]";

    private Type type;
    private String value;
    private int numParam;

    public Token(Type type, String value) {
        this(type, value, 0);
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

    public boolean isVar() {
        return type == Type.VAR;
    }

}
