package at.jku.ssw.java.bytecode.generator.exceptions;

/**
 * Critical {@link RuntimeException} that indicates branches or patterns
 * that should not happen in a normal application workflow.
 * This usually indicates implementation problems.
 */
public class UnexpectedApplicationState extends RuntimeException {
    public UnexpectedApplicationState(String message) {
        super(message);
    }

    public UnexpectedApplicationState() {
        super();
    }
}
