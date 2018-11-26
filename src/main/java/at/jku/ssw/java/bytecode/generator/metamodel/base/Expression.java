package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.VoidType;

/**
 * Describes an expression that eventually evaluates to some result type.
 *
 * @param <T> the type of class that is returned (corresponds to the
 *            {@link MetaType <T>})
 */
@FunctionalInterface
public interface Expression<T> {

    /**
     * Returns the type that this expression evaluates to.
     *
     * @return the type corresponding to the evaluated type
     */
    MetaType<? extends T> type();

    /**
     * Expression that represents "NOP" - i.e. an empty expression.
     */
    enum NOP implements Expression<Void> {

        NOP;

        /**
         * {@inheritDoc}
         */
        @Override
        public MetaType<Void> type() {
            return VoidType.VOID;
        }
    }
}
