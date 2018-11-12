package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import javassist.CtClass;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;

/**
 * Describe types that occur in a generated program such as primitive
 * types, arrays and reference types.
 *
 * @param <T> The actual Java type that is described
 */
public abstract class MetaType<T> {

    //-------------------------------------------------------------------------
    // region Inner classes / types

    /**
     * The available kinds of types. Those represent the actual type
     * in case of primitive values or identify instance and array types.
     * Also, the void type is available.
     */
    public enum Kind {
        BYTE,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        CHAR,
        INSTANCE,
        ARRAY,
        VOID
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * Type category to distinguish between different primitive types,
     * reference types and arrays.
     */
    public final Kind kind;

    /**
     * Optional inner type descriptor for array types.
     */
    public final MetaType<?> inner;

    /**
     * The Java {@link Class} instance corresponding to this type.
     */
    public final Class<T> clazz;

    /**
     * The Javassist {@link CtClass} that maps to this type.
     */
    private final CtClass clazzType;

    /**
     * The number of dimensions for array types (otherwise {@code 0}).
     */
    public final int dim;

    /**
     * Restrictions on access for array types (otherwise {@code null}).
     */
    private final BitSet[] restrictions;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Generates a new type based on the given properties.
     *
     * @param clazz        The actual Java class type instance corresponding to
     *                     this {@link MetaType}.
     * @param clazzType    The {@link CtClass} type that maps to this type
     * @param kind         The kind descriptor to catgorize different types
     * @param inner        Optional inner type reference for array types
     * @param dim          Optional number of dimensions for array types
     * @param restrictions Optional access restrictions for array types.
     *                     Those can be specified for each dimension
     *                     individually.
     */
    protected MetaType(Class<T> clazz,
                       CtClass clazzType,
                       Kind kind,
                       MetaType<?> inner,
                       int dim,
                       BitSet[] restrictions) {

        // iff the type does not have dimensions, it must not be an array
        // and not have an inner type
        // if restrictions are given, it must be an array
        assert (dim == 0) && (inner == null) && (kind != Kind.ARRAY) && (restrictions == null) ||
                (dim > 0) && (inner != null) && (kind == Kind.ARRAY);

        this.kind = kind;
        this.inner = inner;
        this.clazz = clazz;
        this.clazzType = clazzType;
        this.dim = dim;
        this.restrictions = restrictions;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaType<?> that = (MetaType<?>) o;
        return dim == that.dim &&
                kind == that.kind &&
                Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, clazz, dim);
    }

    @Override
    public String toString() {
        if (kind == Kind.VOID)
            return "void";
        return clazz.getCanonicalName();
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Type cast / kind check replacements

    /**
     * Returns a hash that corresponds to this values.
     *
     * @param variable The variable holding this value
     * @return a hash code that identifies this value
     */
    public abstract String getHashCode(FieldVarLogger variable);

    /**
     * Determines whether this type describes a primitive type.
     *
     * @return {@code true} for primitive types; {@code false} otherwise
     */
    public abstract boolean isPrimitive();

    /**
     * Determines whether this type describes a reference type.
     *
     * @return {@code true} for reference types; {@code false} otherwise
     */
    public abstract boolean isRef();


    /**
     * Determines whether this type describes an array type.
     *
     * @return {@code true} for array types; {@code false} otherwise
     */
    public abstract boolean isArray();

    /**
     * Determines whether this type is the {@code void} type.
     *
     * @return {@code true} if this type is {@code void}; {@code false} otherwise
     */
    public abstract boolean isVoid();

    // endregion
    //-------------------------------------------------------------------------
    // region Compatibility methods

    /**
     * Returns all types that are assignable to this type
     * (e.g. all types are assignable from the {@link Object} type
     *
     * @return
     */
    public abstract List<? extends MetaType<?>> getAssignableTypes();

    public abstract boolean isAssignableFrom(MetaType<?> type);

    public boolean isAssignableTo(MetaType<?> type) {
        assert type != null;
        return type.isAssignableFrom(this);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Getters / setters

    public Kind getKind() {
        return kind;
    }

    public MetaType<?> getInner() {
        return inner;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public int getDim() {
        return dim;
    }

    public final CtClass getClazzType() {
        return clazzType;
    }

    public final BitSet[] getRestrictions() {
        return restrictions;
    }

    public final boolean isRestricted() {
        return restrictions != null;
    }

    // endregion
    //-------------------------------------------------------------------------
}
