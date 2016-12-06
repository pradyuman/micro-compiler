package compiler;

public class MicroRuntimeException extends RuntimeException {

    public MicroRuntimeException() { super(); }

    public MicroRuntimeException(String message) { super(message); }

    public MicroRuntimeException(String message, String... state) {
        this(message + appendStateToMessage(state));
    }

    public MicroRuntimeException(Throwable e) { super(e); }

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
