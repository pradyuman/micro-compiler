public class MicroException extends RuntimeException {

    String message;

    public MicroException() { super(); }

    public MicroException(String message) { super(message); }

    public MicroException(Throwable e) { super(e); }
}
