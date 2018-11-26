package at.jku.ssw.java.bytecode.generator.metamodel.base.operations;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.TypeIdentifier;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

import java.util.Collections;
import java.util.List;

/**
 * Describes a method call expression.
 *
 * @param <T> The Java class that describes the result of this method (or void)
 */
public class MethodCall<T> implements Call<T> {

    /**
     * The name of the method.
     */
    private final String name;

    /**
     * The return type.
     */
    private final MetaType<T> type;

    /**
     * The sender (object on whom the method is invoked - the class in case
     * of class methods). May be {@code null} for constructor invocations
     */
    private final Expression<?> sender;

    /**
     * The actual arguments.
     */
    private final List<Expression<?>> arguments;

    /**
     * Creates a new method call expression.
     *
     * @param name      The name of the method
     * @param type      The return type
     * @param sender    The sender expression
     * @param arguments The list of argument expressions
     */
    public MethodCall(String name, MetaType<T> type, Expression<?> sender, List<Expression<?>> arguments) {
        assert name != null;
        assert !name.isEmpty();
        assert type != null;
        assert arguments != null;

        this.name = name;
        this.type = type;
        this.sender = sender;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    /**
     * Returns the name of the method (without sender or arguments).
     *
     * @return a string containing the name of the method
     */
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<T> type() {
        return type;
    }

    /**
     * Returns the sender expression.
     *
     * @return the expression on which this method is called
     */
    public Expression<?> sender() {
        return sender;
    }

    /**
     * Returns the argument list.
     *
     * @return the list of expressions that are passed as arguments
     */
    public List<Expression<?>> arguments() {
        return arguments;
    }

    /**
     * Expression that signals a static method call
     *
     * @param <T> The actual Java class that is returned by this call
     */
    public static class Static<T> extends MethodCall<T> {

        /**
         * Creates a new class method call expression.
         *
         * @param name      The name of the method
         * @param type      The return type
         * @param clazz     The class on which this class method is called
         * @param arguments The parameter list
         */
        public Static(String name,
                      MetaType<T> type,
                      TypeIdentifier<?> clazz,
                      List<Expression<?>> arguments) {

            super(name, type, clazz, arguments);
        }
    }
}
