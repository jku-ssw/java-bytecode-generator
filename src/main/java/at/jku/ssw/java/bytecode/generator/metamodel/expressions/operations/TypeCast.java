package at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations;

import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

/**
 * Operation that casts an expression to a given (registered) type.
 *
 * @param <T> The type that the cast resolves to
 */
public class TypeCast<T> implements Expression<T> {

    /**
     * The type that the expression is cast to.
     */
    private final MetaType<T> target;

    /**
     * The expression that is cast.
     */
    private final Expression<?> param;

    /**
     * Creates a type cast expression with the given type and
     * argument.
     *
     * @param target     The type to which the expression is cast to
     * @param expression The expression that is cast
     */
    public TypeCast(MetaType<T> target, Expression<?> expression) {
        this.target = target;
        this.param = expression;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<T> type() {
        return target;
    }

    /**
     * Returns the single operand.
     *
     * @return the expression that is cast
     */
    public Expression<?> expression() {
        return param;
    }
}
