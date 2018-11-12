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
    // region Constants

    /**
     * Identifier to specifying that a type is
     * dimensionless (i.e. not an array type).
     */
    static final int DIMENSIONLESS = 0;

    /**
     * Identifier to specifying that a type has no inner type
     * (i.e. is no array type).
     */
    static final MetaType<?> NO_INNER_TYPE = null;

    static final BitSet[] UNRESTRICTED = null;

    // endregion
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
     * The Java {@link Class} instance corresponding to this type.
     */
    public final Class<T> clazz;

    /**
     * The Javassist {@link CtClass} that maps to this type.
     */
    private final CtClass clazzType;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Generates a new type based on the given properties.
     *
     * @param clazz     The actual Java class type instance corresponding to
     *                  this {@link MetaType}.
     * @param clazzType The {@link CtClass} type that maps to this type
     * @param kind      The kind descriptor to categorize different types
     */
    protected MetaType(Class<T> clazz,
                       CtClass clazzType,
                       Kind kind) {

        this.kind = kind;
        this.clazz = clazz;
        this.clazzType = clazzType;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaType<?> that = (MetaType<?>) o;
        return getDim() == that.getDim() &&
                kind == that.kind &&
                Objects.equals(clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, clazz, getDim());
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
     * (e.g. all types are assignable from the {@link Object} type).
     *
     * @return a list of assignable types
     */
    public abstract List<? extends MetaType<?>> getAssignableTypes();

    /**
     * Determines whether this type is assignable from the given type.
     * When implementing this method and {@link #getAssignableTypes()},
     * it must hold that this is only assignable from a type
     * iff the given type is also returned as an assignable type in
     * {@link #getAssignableTypes()}.
     *
     * @param type The type to check compatibility with
     * @return {@code true} if this is assignable from the given type;
     * {@code false} otherwise
     */
    public abstract boolean isAssignableFrom(MetaType<?> type);

    /**
     * Determines whether this type is assignable to the given type
     * (e.g. if this is the {@link Object} type, it is only assignable to
     * another {@link Object} type).
     * This check is the complement of {@link #isAssignableFrom(MetaType)},
     * i.e. {@code this.isAssignableFrom(type)} is equivalent to
     * {@code type.isAssignableTo(this)}.
     *
     * @param type The type to check compatibility with
     * @return {@code true} if this type is assignable to the given type;
     * {@code false} otherwise
     */
    public final boolean isAssignableTo(MetaType<?> type) {
        assert type != null;
        return type.isAssignableFrom(this);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Array type methods

    /**
     * Returns the {@link MetaType} that acts as a component type (if any)
     * for this type.
     * E.g. if this type describes an array {@code int[][]}, the
     * inner type would be the {@link PrimitiveType} for {@code int}.
     *
     * @return the component type of this type or {@code null} if this type
     * is no array type
     */
    public MetaType<?> getInner() {
        return NO_INNER_TYPE;
    }

    /**
     * Returns the number of dimensions that this type is equipped with.
     * This number must not be negative.
     *
     * @return a strictly positive number of dimensions for array types;
     * {@code 0} for non-array types
     */
    public int getDim() {
        return DIMENSIONLESS;
    }

    /**
     * Returns access restrictions that apply to this type.
     * When overridden, the condition must hold that if {@link #getDim()}
     * is greater than zero, it must be equal to the length of the
     * array returned here.
     *
     * @return an array that describes the access restrictions for each
     * dimension of an array type; {@code null} for non-array types
     */
    public BitSet[] getRestrictions() {
        return UNRESTRICTED;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Getters / setters

    /**
     * Returns the kind associated with this type.
     *
     * @return the kind that categorizes this type
     */
    public final Kind getKind() {
        return kind;
    }

    /**
     * Returns the actual Java class that corresponds to this type.
     *
     * @return the Java class which is described by this type
     */
    public final Class<T> getClazz() {
        return clazz;
    }

    /**
     * Returns the Javassist representation of the Java class given by
     * {@link #getClazz()}.
     *
     * @return the Javassist pendant to the Java class
     */
    public final CtClass getClazzType() {
        return clazzType;
    }

    /**
     * Determines whether this type implies access restrictions
     * (e.g. access array only at index 1 in the first dimension).
     *
     * @return {@code true} if this type has restrictions;
     * {@code false} otherwise
     */
    public final boolean isRestricted() {
        return getRestrictions() != null;
    }

    // endregion
    //-------------------------------------------------------------------------
}
