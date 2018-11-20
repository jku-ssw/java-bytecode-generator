package at.jku.ssw.java.bytecode.generator.metamodel.base;

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
public class ArrayInit<T> implements Expression<T> {

    /**
     * The actual array meta type.
     */
    private final ArrayType<T> type;

    /**
     * Parameters that this array initialization takes.
     * E.g. {@code new int[1][2]} takes two parameters.
     * All parameter types must implicitly be of type {@code int}
     */
    private final List<? extends Expression<?>> params;

    public ArrayInit(ArrayType<T> type, List<? extends Expression<?>> params) {
        assert type.getDim() == params.size() : "The number of expressions forming the parameter list must be equal to the number of dimensions";

        // all parameter types must be assignable to integer
        assert params.stream().allMatch(p -> p.type().isAssignableTo(PrimitiveType.INT));

        this.type = type;
        this.params = Collections.unmodifiableList(params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<T> type() {
        return type;
    }

    public List<? extends Expression<?>> getParams() {
        return params;
    }
}
