package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.metamodel.base.constants.NullConstant;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

/**
 * Null builder that is reusable for all instance types.
 * Is also used as a null constant.
 *
 * @param <T> the type of value by which {@code null} is represented
 */
public final class NullBuilder<T> implements Builder.NoArgs<T> {

    /**
     * The type to which this null value corresponds
     */
    private final RefType<T> type;

    /**
     * Creates a new {@link NullBuilder} for the given meta type.
     * The type has to be provided to defer the correct method in case of
     * ambiguous method calls.
     *
     * @param type The type for which the null value is registered
     */
    public NullBuilder(RefType<T> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefType<T> returns() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NullConstant<T> build() {
        return new NullConstant<>(type);
    }
}
