package at.jku.ssw.java.bytecode.generator.metamodel.expressions.constants;

import at.jku.ssw.java.bytecode.generator.types.base.RefType;

/**
 * Null constant.
 *
 * @param <T> the type of value by which {@code null} is represented
 */
public final class NullConstant<T> implements Constant<T> {

    private final RefType<T> type;

    public NullConstant(RefType<T> type) {
        this.type = type;
    }

    @Override
    public T value() {
        return null;
    }

    @Override
    public RefType<T> type() {
        return type;
    }
}
