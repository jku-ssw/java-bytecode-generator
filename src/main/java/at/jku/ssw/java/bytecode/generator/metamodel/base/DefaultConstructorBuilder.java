package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

/**
 * Builder for default constructors of arbitrary type.
 *
 * @param <T> The actual Java class that is returned
 */
public class DefaultConstructorBuilder<T> implements Builder.NoArgs<T> {

    /**
     * The type to which this constructor corresponds
     */
    private final RefType<T> type;

    /**
     * Creates a new default constructor builder for the given reference type.
     *
     * @param type The type to build
     */
    public DefaultConstructorBuilder(RefType<T> type) {
        this.type = type;
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
    public Expression<T> build() {
        return new ConstructorCall<>(type);
    }
}
