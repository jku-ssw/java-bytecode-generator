package at.jku.ssw.java.bytecode.generator.metamodel.base;

import java.util.List;

/**
 * Declares an instantiable type.
 */
public interface Instantiable<T> {

    /**
     * The "builder" that are available for this type.
     * These are options on how to initialize instance of this type.
     * This includes direct initialization for primitive values - e.g.
     * {@code int i = 1} - but also object instantiation via constructors,
     * the assignment of string literals or initialization with {@code null}.
     *
     * @return a list of builders that are applicable for this type
     */
    List<Builder<T>> builders();
}
