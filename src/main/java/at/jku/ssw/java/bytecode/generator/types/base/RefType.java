package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.DefaultConstructorBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Meta type which generally describes reference types such as objects
 * of various kinds and arrays.
 *
 * @param <T> The actual Java class associated with this type
 */
public interface RefType<T> extends MetaType<T> {
    //-------------------------------------------------------------------------
    // region Properties

    @Override
    default Kind kind() {
        return Kind.INSTANCE;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region MetaType methods

    /**
     * {@inheritDoc}
     */
    @Override
    default boolean isAssignableFrom(MetaType<?> other) {
        return other instanceof RefType && clazz().isAssignableFrom(other.clazz());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes()
                .filter(this::isAssignableFrom)
                .collect(Collectors.toList());
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Builder methods

    /**
     * {@inheritDoc}
     */
    @Override
    default List<Builder<T>> builders() {
        // TODO dynamically determine (via reflection?)!
        return asList(
                new NullBuilder<>(this),
                new DefaultConstructorBuilder<>(this)
        );
    }

    // endregion
    //-------------------------------------------------------------------------
}
