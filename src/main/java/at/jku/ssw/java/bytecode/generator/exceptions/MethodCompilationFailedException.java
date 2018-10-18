package at.jku.ssw.java.bytecode.generator.exceptions;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;

public class MethodCompilationFailedException extends CompilationFailedException {

    private static final String MESSAGE_FORMAT = "Compilation of '%s' failed";
    private static final String MESSAGE_FORMAT_LONG = "Compilation of '%s' failed: %s";

    public MethodCompilationFailedException(MethodLogger method, String message, Throwable cause) {
        super(String.format(MESSAGE_FORMAT_LONG, method, message), cause);
    }

    public MethodCompilationFailedException(MethodLogger method, Throwable cause) {
        super(String.format(MESSAGE_FORMAT, method), cause);
    }

    public MethodCompilationFailedException(MethodLogger method, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(String.format(MESSAGE_FORMAT_LONG, method, message), cause, enableSuppression, writableStackTrace);
    }
}
