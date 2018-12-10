package at.jku.ssw.java.bytecode.generator.metamodel.builders;

import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.util.Arrays;
import java.util.List;

/**
 * Builder for constructors.
 *
 * @param <T> The actual Java class of the corresponding instance
 */
public class ConstructorBuilder<T> implements Builder<T> {
    /**
     * The type to which this constructor corresponds.
     */
    protected final RefType<T> type;

    /**
     * The required constructor arguments.
     */
    protected final List<? extends MetaType<?>> paramTypes;

    /**
     * Creates a new constructor builder for the given reference type
     * and parameters.
     *
     * @param type       The type to build
     * @param paramTypes The required constructor arguments
     */
    public ConstructorBuilder(RefType<T> type, List<? extends MetaType<?>> paramTypes) {
        this.type = type;
        this.paramTypes = paramTypes;
    }

    /**
     * @see #ConstructorBuilder(RefType, List)
     */
    public ConstructorBuilder(RefType<T> type, MetaType<?>... paramTypes) {
        this(type, Arrays.asList(paramTypes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<T> returns() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<T> build(List<? extends Expression<?>> params) {
        return new ConstructorCall<>(type, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends MetaType<?>> requires() {
        return paramTypes;
    }

}

