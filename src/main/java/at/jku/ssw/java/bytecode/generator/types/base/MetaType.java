package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Instantiable;
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
public interface MetaType<T> extends Instantiable<T> {
    //-------------------------------------------------------------------------
    // region Constants

    /**
     * Identifier to specifying that a type is
     * dimensionless (i.e. not an array type).
     */
    int DIMENSIONLESS = 0;

    /**
     * Identifier to specifying that a type has no inner type
     * (i.e. is no array type).
     */
    MetaType<?> NO_INNER_TYPE = null;

    BitSet[] UNRESTRICTED = null;

    // endregion
    //-------------------------------------------------------------------------
    // region Inner classes / types

    /**
     * The available kinds of types. Those represent the actual type
     * in case of primitive values or identify instance and array types.
     * Also, the void type is available.
     */
    enum Kind {
        BYTE,
        SHORT,
        RINT,
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
     * Returns the kind associated with this type.
     * This kind represents the type category to distinguish between
     * different primitive types, reference types and arrays.
     *
     * @return the kind that categorizes this type
     */
    Kind kind();

    /**
     * Returns the actual Java {@link Class} instance that corresponds to
     * this type.
     *
     * @return the Java class which is described by this type
     */
    Class<T> clazz();

    /**
     * Returns the Javassist {@link CtClass} representation of the Java class
     * given by {@link #clazz()}.
     *
     * @return the Javassist pendant to the Java class
     */
    CtClass javassistClazz();

    /**
     * Determines whether this type implies access restrictions
     * (e.g. access array only at index 1 in the first dimension).
     *
     * @return {@code true} if this type has restrictions;
     * {@code false} otherwise
     */
    default boolean isRestricted() {
        return getRestrictions() != null;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    /**
     * Compares this instance to another type.
     *
     * @param o The other type
     * @return {@code true} if the type kinds, the mapped classes and the
     * dimensions match; {@code false} otherwise
     * @see Object#equals(Object)
     */
    default boolean equals(MetaType<?> o) {
        return getDim() == o.getDim() &&
                kind() == o.kind() &&
                Objects.equals(clazz(), o.clazz());
    }

    /**
     * Generates a hash code that corresponds to this instance.
     *
     * @return a hash value that uniquely identifies this instance.
     * @see Object#hashCode()
     */
    default int hash() {
        return Objects.hash(kind(), clazz(), getDim());
    }

    /**
     * Returns the descriptor of this type.
     *
     * @return the string representation of this type
     * @see Object#toString()
     */
    default String name() {
        return clazz().getCanonicalName();
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
    String getHashCode(FieldVarLogger<T> variable);

    /**
     * Determines whether this type describes a primitive type.
     *
     * @return {@code true} for primitive types; {@code false} otherwise
     */
    boolean isPrimitive();

    /**
     * Determines whether this type describes a reference type.
     *
     * @return {@code true} for reference types; {@code false} otherwise
     */
    boolean isRef();


    /**
     * Determines whether this type describes an array type.
     *
     * @return {@code true} for array types; {@code false} otherwise
     */
    boolean isArray();

    /**
     * Determines whether this type is the {@code void} type.
     *
     * @return {@code true} if this type is {@code void}; {@code false} otherwise
     */
    boolean isVoid();

    // endregion
    //-------------------------------------------------------------------------
    // region Compatibility methods

    /**
     * Returns all types that are assignable to this type
     * (e.g. all types are assignable from the {@link Object} type).
     *
     * @return a list of assignable types
     */
    List<? extends MetaType<?>> getAssignableTypes();

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
    boolean isAssignableFrom(MetaType<?> type);

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
    default boolean isAssignableTo(MetaType<?> type) {
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
    default MetaType<?> getInner() {
        return NO_INNER_TYPE;
    }

    /**
     * Returns the number of dimensions that this type is equipped with.
     * This number must not be negative.
     *
     * @return a strictly positive number of dimensions for array types;
     * {@code 0} for non-array types
     */
    default int getDim() {
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
    default BitSet[] getRestrictions() {
        return UNRESTRICTED;
    }

    // endregion
    //-------------------------------------------------------------------------
}
