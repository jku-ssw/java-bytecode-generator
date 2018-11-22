package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.exceptions.UnexpectedApplicationState;

/**
 * Utilities regarding error handling and simplified throwing mechanisms.
 */
public class ErrorUtils {
    public static UnexpectedApplicationState shouldNotReachHere(String msg) {
        return new UnexpectedApplicationState(msg);
    }

    public static UnexpectedApplicationState shouldNotReachHere() {
        return new UnexpectedApplicationState();
    }
}
