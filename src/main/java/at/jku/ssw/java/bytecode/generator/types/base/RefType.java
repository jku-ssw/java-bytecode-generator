package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.DefaultConstructorBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind.INSTANCE;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Meta type which generally describes reference types such as objects
 * of various kinds and arrays.
 *
 * @param <T> The actual Java class associated with this type
 */
public class RefType<T> implements MetaType<T> {
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The described Java class.
     */
    private final Class<T> clazz;

    /**
     * The Javassist equivalent to {@link #clazz}
     */
    private final CtClass javassistClass;

    /**
     * The kind of reference type that is described
     * (either {@link at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind#ARRAY} or
     * {@link at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind#INSTANCE}).
     */
    private final Kind kind;

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return kind;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> clazz() {
        return clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CtClass javassistClazz() {
        return javassistClass;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Generates a new reference type by inferring the remaining properties
     * from the given class type.
     *
     * @param clazz The actual Java class type instance corresponding to
     *              this {@link MetaType}.
     */
    protected RefType(Class<T> clazz) {
        this(clazz, INSTANCE);
    }

    /**
     * Creates a new reference type with the given properties.
     *
     * @param clazz The actual Java class type instance.
     * @param kind  The kind of the type
     */
    protected RefType(Class<T> clazz, Kind kind) {
        assert clazz != null;
        assert kind != null;
        assert !clazz.isPrimitive();

        this.clazz = clazz;
        this.javassistClass = JavassistUtils.toCtClass(clazz);
        this.kind = kind;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equals((RefType<?>) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return hash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<T> variable) {
        String name = variable.access();

        if (clazz().equals(String.class)) {
            return ternary(
                    notNull(name),
                    method(name, "hashCode"),
                    "0L"
            );
        } else if (clazz().equals(Date.class)) {
            return ternary(
                    notNull(name),
                    method(name, "getTime"),
                    "0L"
            );
        }

        // otherwise get the hash code of the class name
        return ternary(
                notNull(name),
                method(method(method(name, "getClass"), "getSimpleName"), "hashCode"),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        return other instanceof RefType && clazz().isAssignableFrom(other.clazz());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
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
    public List<Builder<T>> builders() {
        // TODO dynamically determine (via reflection?)!
        return Arrays.asList(
                new NullBuilder<>(this),
                new DefaultConstructorBuilder<>(this)
        );
    }

    // endregion
    //-------------------------------------------------------------------------
}
