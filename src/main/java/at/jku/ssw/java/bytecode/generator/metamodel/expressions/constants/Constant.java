package at.jku.ssw.java.bytecode.generator.metamodel.expressions.constants;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;

/**
 * Interface of constant expressions.
 *
 * @param <T> The actual Java type
 */
public interface Constant<T> extends Expression<T> {

    /**
     * The constant value.
     *
     * @return the constant value of the given type
     */
    T value();
}
