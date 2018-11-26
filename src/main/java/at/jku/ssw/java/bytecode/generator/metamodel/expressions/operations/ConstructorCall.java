package at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a constructor call expression including parameters.
 *
 * @param <T> The actual Java type
 */
public class ConstructorCall<T> implements Call<T> {

    /**
     * The name convention for constructors.
     */
    public static final String METHOD_NAME = "<init>";

    /**
     * The reference type that is invoked.
     */
    private final RefType<T> type;

    /**
     * The actual arguments.
     */
    private final List<Expression<?>> arguments;

    /**
     * Creates a new constructor calls expression for the given type
     * using the given parameter list.
     *
     * @param type      The reference type that is initialized
     * @param arguments The list of arguments that this constructor is invoked on
     */
    public ConstructorCall(RefType<T> type, List<Expression<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    /**
     * Creates a new default constructor call.
     *
     * @param type The reference type that is instantiated
     */
    public ConstructorCall(RefType<T> type) {
        this(type, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefType<T> type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return METHOD_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<?> sender() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Expression<?>> arguments() {
        return arguments;
    }
}
