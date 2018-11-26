package at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.Operator;

/**
 * Generic superclass for expressions describing binary operations.
 *
 * @param <T> The resulting type
 */
public class BinaryOp<T> implements Expression<T> {

    /**
     * The type that this operation resolves to.
     */
    private final MetaType<T> type;

    /**
     * The operator.
     */
    private final Operator op;

    /**
     * The first operand.
     */
    private final Expression<?> a;

    /**
     * The second operand.
     */
    private final Expression<?> b;

    /**
     * Creates a new binary operation.
     *
     * @param type The target type
     * @param op   The operation
     * @param a    The first operand
     * @param b    The second operand
     */
    public BinaryOp(MetaType<T> type, Operator op, Expression<?> a, Expression<?> b) {
        this.type = type;
        this.op = op;
        this.a = a;
        this.b = b;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<? extends T> type() {
        return type;
    }

    /**
     * Returns the operation.
     *
     * @return a value that describes the operation that is actually applied
     */
    public Operator op() {
        return op;
    }

    /**
     * Returns the first operand.
     *
     * @return the expression that forms the first operand
     */
    public Expression<?> a() {
        return a;
    }

    /**
     * Returns the second operand.
     *
     * @return the expression that forms the second operand
     */
    public Expression<?> b() {
        return b;
    }
}
