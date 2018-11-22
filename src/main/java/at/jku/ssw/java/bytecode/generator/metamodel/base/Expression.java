package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

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
}
