package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.types.MetaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
     * Specifies the number of parameters that are required to invoke this
     * builder. Serves as an alias for {@code requires().size()}.
     *
     * @return the required number of parameters
     */
    default int nArgs() {
        return requires().size();
    }

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
    Expression<T> build(List<Expression<?>> params);

    /**
     * @see #build(List)
     */
    default Expression<T> build(Expression<?>... params) {
        return build(Arrays.asList(params));
    }

    /**
     * Specifies the return type of this builder.
     *
     * @return a meta type describing the result of this builder.
     */
    MetaType<T> returns();

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
        default Expression<T> build(List<Expression<?>> __) {
            return build();
        }

        /**
         * @see #build(Expression[])
         */
        Expression<T> build();
    }

}
