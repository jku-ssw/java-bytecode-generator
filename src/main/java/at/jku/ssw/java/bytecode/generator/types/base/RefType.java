package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.DefaultConstructorBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.LibMethod;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

/**
 * Meta type which generally describes reference types such as objects
 * of various kinds and arrays.
 *
 * @param <T> The actual Java class associated with this type
 */
public interface RefType<T> extends MetaType<T> {
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * {@inheritDoc}
     */
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
        return TypeCache.CACHE.refTypes()
                .filter(this::isAssignableFrom)
                .collect(Collectors.toList());
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Builder methods

    /**
     * Returns all methods that may be invoked from this type.
     *
     * @return a list of {@link MethodBuilder}s that includes both
     * library methods and generated methods
     */
    List<? extends MethodBuilder<?>> methods();

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

    /**
     * Helper that allows subtypes to automatically infer their public library
     * methods. It excludes those that are specified by the subtype
     * as well as some default ones (e.g. {@link Object#hashCode()},
     * {@link Object#finalize()}.
     *
     * @return a list of wrapped library methods.
     */
    default List<LibMethod<?>> inferMethods() {
        return Arrays.stream(clazz().getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !excludedLibraryMethods().contains(m.toString()))
                .map(LibMethod::infer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Allows implementations to define exclusions when infering their
     * library methods.
     *
     * @return a list of strings that describe excluded methods
     * @see java.lang.reflect.Method#toString()
     */
    default Set<String> excludedLibraryMethods() {
        return emptySet();
    }
}
