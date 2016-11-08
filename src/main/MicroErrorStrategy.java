package main;

import org.antlr.v4.runtime.*;

public class MicroErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new MicroException(e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new MicroException();
    }

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        throw new MicroException();
    }

    @Override
    public void sync(Parser recognizer) { }

}
