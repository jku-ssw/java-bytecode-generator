package at.jku.ssw.java.bytecode.generator.exceptions;

/**
 * {@link RuntimeException} type that indicates that a generated code
 * sample could not be compiled.
 */
public class CompilationFailedException extends RuntimeException {
    public CompilationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompilationFailedException(Throwable cause) {
        super(cause);
    }

    public CompilationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
