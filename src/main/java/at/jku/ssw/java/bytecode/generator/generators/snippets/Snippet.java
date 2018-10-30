package at.jku.ssw.java.bytecode.generator.generators.snippets;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;

/**
 * Represents the generic structure of predefined code snippets
 * that may be randomly inserted.
 */
public interface Snippet {

    /**
     * Checks whether this snippet is applicable for this method.
     *
     * @param method The method for which this snippet may be generated
     * @return {@code true} if this snippet may be generated for this method;
     * {@code false} otherwise
     */
    boolean isPossible(MethodLogger method);

    /**
     * Creates the snippet code.
     *
     * @param randomSupplier A random supplier that allows to fetch
     *                       random values
     * @return a string containing the generated source code
     */
    String generate(RandomSupplier randomSupplier);
}
