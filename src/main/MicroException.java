package main;

public class MicroException extends RuntimeException {

    public MicroException() { super(); }

    public MicroException(String message) { super(message); }

    public MicroException(String message, String... state) {
        this(message + appendStateToMessage(state));
    }

    public MicroException(Throwable e) { super(e); }

    private static String appendStateToMessage(String... state) {
        String message = "";
        if (state.length != 0) {
            StringBuilder b = new StringBuilder();
            b.append(": ");
            for (String s : state)
                b.append(s + ", ");
            message += b.toString();
        }
        return message;
    }
}
