package at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

import java.util.Collections;
import java.util.List;

/**
 * Represents an array initialization.
 *
 * @param <T> The Java type corresponding to the array
 */
public class ArrayInit<T> implements Call<T> {

    public static final String METHOD_NAME = "arrayinit";

    /**
     * The actual array meta type.
     */
    private final ArrayType<T> type;

    /**
     * Parameters that this array initialization takes.
     * E.g. {@code new int[1][2]} takes two parameters.
     * All parameter types must implicitly be of type {@code int}
     */
    private final List<Expression<?>> arguments;

    /**
     * Creates a new array initalization expression.
     *
     * @param type      The type of array to create
     * @param arguments The parameters (dimension lengths)
     */
    public ArrayInit(ArrayType<T> type, List<Expression<?>> arguments) {
        assert type.getDim() == arguments.size() : "The number of expressions forming the parameter list must be equal to the number of dimensions";

        // all parameter types must be assignable to integer
        assert arguments.stream().allMatch(p -> p.type().isAssignableTo(PrimitiveType.INT));

        this.type = type;
        this.arguments = Collections.unmodifiableList(arguments);
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
    public MetaType<T> type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Expression<?>> arguments() {
        return arguments;
    }
}
