package at.jku.ssw.java.bytecode.generator.metamodel;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Defines one way to build a value of the given type and specifies the
 * parameters that it requires.
 * Those methods include constructor invocations, generation via methods
 * or direct initialization of primitive types.
 *
 * @param <T> The type that is produced by this builder
 */
public interface Builder<T> {

    /**
     * Specifies the required parameter types to invoke this builder.
     * This list is either empty or filled with the corresponding types
     * but should never be zero.
     *
     * @return the required parameter types
     */
    List<? extends MetaType<?>> requires();

    /**
     * Executes this builder with the given parameters and
     * returns the required result.
     * The given parameter expressions must correspond to the
     * types specified in {@link #requires()}.
     *
     * @param params The parameters that this builder is invoked with
     * @return the built type
     */
    Expression<T> build(List<? extends Expression<?>> params);

    /**
     * Specifies the return type of this builder.
     *
     * @return a meta type describing the result of this builder.
     */
    MetaType<T> returns();

    /**
     * Excludes calls to the given builder from this builder.
     * Unless overridden, this does nothing.
     *
     * @param builder The other builder that must not be called
     *                from this builder
     * @return this builder
     */
    default Builder<T> exclude(Builder<?> builder) {
        return this;
    }

    /**
     * Defines builders that are excluded from being called from within
     * this builder (to avoid recursions).
     *
     * @return a set of builders that are excluded
     */
    default Set<? extends Builder<?>> exclusions() {
        return emptySet();
    }

    /**
     * Checks whether this builder is excluded from being called from this
     * builder. This is checked to prevent the generation of recursions.
     *
     * @param builder The builder to check
     * @return {@code true} if this builder is excluded in either this builder
     * or in any other builder in the hierarchy; {@code false} otherwise
     * @see #isAllowed(Builder)
     */
    default boolean isExcluded(Builder<?> builder) {
        Set<? extends Builder<?>> exclusions = exclusions();

        return this == builder ||
                exclusions.contains(builder) ||
                exclusions.stream().anyMatch(b -> b.isExcluded(builder));
    }

    /**
     * Checks whether the given builder is allowed to be called from within
     * this builder.
     *
     * @param builder The builder to be called
     * @return {@code true} if the given builder is not called from this
     * builder or any builder that is excluded
     * @see #isExcluded(Builder)
     */
    default boolean isAllowed(Builder<?> builder) {
        return !isExcluded(builder);
    }

    /**
     * Helper for no-argument builders (no argument constructors).
     *
     * @param <T> The mapped Java class
     */
    interface NoArgs<T> extends Builder<T> {

        /**
         * {@inheritDoc}
         */
        @Override
        default List<? extends MetaType<?>> requires() {
            return Collections.emptyList();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        default Expression<T> build(@SuppressWarnings("unused") List<? extends Expression<?>> __) {
            return build();
        }

        /**
         * @see #build(List)
         */
        Expression<T> build();
    }

}
