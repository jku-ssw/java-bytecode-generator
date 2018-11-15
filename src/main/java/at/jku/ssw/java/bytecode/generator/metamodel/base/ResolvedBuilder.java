package at.jku.ssw.java.bytecode.generator.metamodel.base;

import java.util.List;

/**
 * Utility to combine {@link Builder} instances with corresponding
 * expressions that may be used to invoke it (e.g. constants, variable access
 * or other expressions).
 *
 * @param <T> The actual Java class that this builder results in
 */
public class ResolvedBuilder<T> {
    /**
     * The builder instance that is used.
     */
    public final Builder<T> builder;

    /**
     * The actual parameter expressions for the builder.
     */
    public final List<Expression<?>> params;

    /**
     * Creates a new resolved builder for the given builder and the given
     * parameter expressions that are used to generate the final expression.
     *
     * @param builder The builder instance
     * @param params  The list of parameter expressions
     */
    public ResolvedBuilder(Builder<T> builder, List<Expression<?>> params) {
        this.builder = builder;
        this.params = params;
    }
}
