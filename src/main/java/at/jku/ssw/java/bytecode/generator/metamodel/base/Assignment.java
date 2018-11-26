package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

/**
 * Represents a generic assignment.
 *
 * @param <T> The type that is assigned
 */
public class Assignment<T> implements Expression<T> {

    /**
     * The variable that the expression is assigned to.
     */
    private final FieldVarLogger<T> dest;

    /**
     * The source expression.
     */
    private final Expression<T> src;

    /**
     * Creates a new assignment expression.
     *
     * @param dest The assignment target
     * @param src  The source expression
     */
    public Assignment(FieldVarLogger<T> dest, Expression<T> src) {
        assert dest != null;
        assert src != null;

        this.dest = dest;
        this.src = src;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<? extends T> type() {
        return dest.getType();
    }

    /**
     * Returns the target variable.
     *
     * @return the field or variable instance that the source expression
     * is assigned to
     */
    public FieldVarLogger<T> dest() {
        return dest;
    }

    /**
     * Returns the source expression
     *
     * @return the expression that is assigned
     */
    public Expression<T> src() {
        return src;
    }
}
