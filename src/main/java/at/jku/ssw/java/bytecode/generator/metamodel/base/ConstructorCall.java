package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.types.RefType;

import java.util.Collections;
import java.util.List;

/**
 * Represents a constructor call expression including parameters.
 *
 * @param <T> The actual Java type
 */
public class ConstructorCall<T> implements Expression<T> {

    private final RefType<T> type;
    private final List<Expression<?>> params;

    public ConstructorCall(RefType<T> type, List<Expression<?>> params) {
        this.type = type;
        this.params = Collections.unmodifiableList(params);
    }

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

    public List<Expression<?>> getParams() {
        return params;
    }
}
