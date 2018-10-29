package at.jku.ssw.java.bytecode.generator.utils;

import javassist.CtClass;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.Kind.ARRAY;
import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.Kind.INSTANCE;

public class FieldVarType<T> {
    //-------------------------------------------------------------------------
    // region constants

    public static final int MIN_ARRAY_DIM_LENGTH = 10;

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
    // region Type management

    /**
     * Instance container that stores any object of type {@link FieldVarType}
     * that is created. Is used to retrieve random types.
     */
    private static final Set<FieldVarType<?>> types = new HashSet<>();

    private static <T> FieldVarType<T> register(FieldVarType<T> type) {
        assert types != null;
        boolean notExists = types.add(type);
        assert notExists : "Type '" + type + "' already registered";
        return type;
    }

    // rendregion
    //-------------------------------------------------------------------------
    // region Type declarations

    public static final FieldVarType<Byte> BYTE = register(of(byte.class, CtClass.byteType, Kind.BYTE));
    public static final FieldVarType<Short> SHORT = register(of(short.class, CtClass.shortType, Kind.SHORT));
    public static final FieldVarType<Integer> INT = register(of(int.class, CtClass.intType, Kind.INT));
    public static final FieldVarType<Long> LONG = register(of(long.class, CtClass.longType, Kind.LONG));
    public static final FieldVarType<Float> FLOAT = register(of(float.class, CtClass.floatType, Kind.FLOAT));
    public static final FieldVarType<Double> DOUBLE = register(of(double.class, CtClass.doubleType, Kind.DOUBLE));
    public static final FieldVarType<Boolean> BOOLEAN = register(of(boolean.class, CtClass.booleanType, Kind.BOOLEAN));
    public static final FieldVarType<Character> CHAR = register(of(char.class, CtClass.charType, Kind.CHAR));
    @SuppressWarnings("unused")
    public static final FieldVarType<String> STRING = register(of(String.class));
    @SuppressWarnings("unused")
    public static final FieldVarType<Date> DATE = register(of(Date.class));
    public static final FieldVarType<Void> VOID = register(of(Void.class, CtClass.voidType, Kind.VOID));

    // endregion
    //-------------------------------------------------------------------------
    // region Type compatibility sets

    private static final List<FieldVarType<?>> NUMERIC_TYPES = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.CHAR,
            FieldVarType.DOUBLE,
            FieldVarType.FLOAT,
            FieldVarType.INT,
            FieldVarType.LONG,
            FieldVarType.SHORT
    );

    private static final List<FieldVarType<?>> COMP_WITH_SHORT = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR
    );

    private static final List<FieldVarType<?>> COMP_WITH_INT = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR,
            FieldVarType.INT
    );

    private static final List<FieldVarType<?>> COMP_WITH_LONG = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR,
            FieldVarType.INT,
            FieldVarType.LONG
    );

    private static final List<FieldVarType<?>> COMP_WITH_DOUBLE = Arrays.asList(
            FieldVarType.FLOAT,
            FieldVarType.DOUBLE
    );

    // endregion
    //-------------------------------------------------------------------------
    // region Type accessors

    public static List<FieldVarType<?>> numericTypes() {
        return NUMERIC_TYPES;
    }

    public static Stream<FieldVarType<?>> types() {
        return types.stream();
    }

    public static Stream<FieldVarType<?>> primitiveTypes() {
        return types()
                .filter(t -> t.kind != INSTANCE)
                .filter(t -> t.kind != Kind.VOID)
                .filter(t -> t.kind != Kind.ARRAY);
    }

    public static Stream<FieldVarType<?>> classTypes() {
        return types()
                .filter(t -> t.kind == INSTANCE);
    }

    public static FieldVarType<?> arrayTypeOf(FieldVarType<?> type, int dim) {
        return FieldVarType.arrayTypeOf(type, dim, null);
    }

    public static FieldVarType<?> arrayTypeOf(FieldVarType<?> type, int dim, BitSet[] restrictions) {
        assert type != null : "Array type must not be null";
        assert type.kind != Kind.VOID : "Cannot create array of void type";
        assert dim > 0 : "Invalid array dimensions";

        final String desc;
        switch (type.kind) {
            case BYTE:
                desc = "B";
                break;
            case SHORT:
                desc = "S";
                break;
            case INT:
                desc = "I";
                break;
            case LONG:
                desc = "J";
                break;
            case FLOAT:
                desc = "F";
                break;
            case DOUBLE:
                desc = "D";
                break;
            case BOOLEAN:
                desc = "Z";
                break;
            case CHAR:
                desc = "C";
                break;
            case INSTANCE:
                desc = "L" + type.clazz.getCanonicalName() + ";";
                break;
            default:
                // should not occur
                desc = null;
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(
                    IntStream.range(0, dim)
                            .mapToObj(__ -> "[")
                            .collect(Collectors.joining()) + desc
            );
        } catch (ClassNotFoundException e) {
            // should not happen
            throw new AssertionError(e);
        }

        return of(clazz, dim, type, restrictions);
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
    public final FieldVarType<?> inner;

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
     * Initializes a new reference type based on the given class.
     *
     * @param clazz The class to base the reference type on
     * @param <T>   The type corresponding to the Java class type
     */
    public static <T> FieldVarType<T> of(Class<T> clazz) {
        return of(
                clazz,
                JavassistUtils.toCtClass(clazz),
                INSTANCE
        );
    }

    /**
     * Initializes an array type.
     *
     * @param clazz        The array type descriptor
     *                     (e.g. an instance of {@code Class<int[]>})
     * @param dim          The number of dimensions of the array type
     * @param inner        The inner field type (e.g. {@link FieldVarType#INT})
     * @param restrictions Optional restrictions on the access range
     *                     (e.g. only access dimension 0 at positions 3 to 5)
     */
    public static <T> FieldVarType<T> of(Class<T> clazz, int dim, FieldVarType<?> inner, BitSet[] restrictions) {
        return new FieldVarType<>(
                clazz,
                JavassistUtils.toCtClass(clazz),
                ARRAY,
                inner,
                dim,
                restrictions
        );
    }

    /**
     * @see #of(Class, int, FieldVarType, BitSet[])
     */
    public static <T> FieldVarType<T> of(Class<T> clazz, int dim, FieldVarType<?> inner) {
        return of(clazz, dim, inner, null);
    }

    /**
     * Initializes special types that are backed by specific {@link CtClass}
     * type constants (i.e. primitive types, void).
     *
     * @param clazz     The corresponding class (e.g. {@code int.class})
     * @param clazzType The mapped {@link CtClass} type instance
     *                  (e.g. {@link CtClass#intType}.
     * @param kind      The kind of the type
     */
    public static <T> FieldVarType<T> of(Class<T> clazz, CtClass clazzType, Kind kind) {
        return new FieldVarType<>(clazz, clazzType, kind, null, 0, null);
    }

    /**
     * Generates a new type based on the given properties.
     *
     * @param clazz        The actual Java class type instance corresponding to
     *                     this {@link FieldVarType}.
     * @param clazzType    The {@link CtClass} type that maps to this type
     * @param kind         The kind descriptor to catgorize different types
     * @param inner        Optional inner type reference for array types
     * @param dim          Optional number of dimensions for array types
     * @param restrictions Optional access restrictions for array types.
     *                     Those can be specified for each dimension
     *                     individually.
     */
    public FieldVarType(Class<T> clazz, CtClass clazzType, Kind kind, FieldVarType<?> inner, int dim, BitSet[] restrictions) {
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
        FieldVarType<?> that = (FieldVarType<?>) o;
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
    // region Public utility methods

    /**
     * Checks whether this type is assignable from the given type -
     * e.g. {@code Object} being assignable from anything
     *
     * @param other The other type whose compatibility is checked
     * @return {@code true} if the type is assignable to this type;
     * {@code false} otherwise
     */
    public boolean isAssignableFrom(FieldVarType<?> other) {
        switch (kind) {
            case INSTANCE:
                return clazz.isAssignableFrom(other.clazz);
            case DOUBLE:
                if (other.kind == Kind.DOUBLE) return true;
            case FLOAT:
                return other.kind == Kind.FLOAT;
            case LONG:
                if (other.kind == Kind.LONG) return true;
            case INT:
                if (other.kind == Kind.INT) return true;
            case SHORT:
                return other.kind == Kind.SHORT || other.kind == Kind.CHAR || other.kind == Kind.BYTE;
            case CHAR:
                return other.kind == Kind.CHAR;
            case BOOLEAN:
                return other.kind == Kind.BOOLEAN;
            case BYTE:
                return other.kind == Kind.BYTE;
            default:
                return false;
        }
    }

    /**
     * Returns the types that are assignable to this type.
     *
     * @return a list of types that are assignable to this type
     */
    public List<FieldVarType<?>> getAssignableTypes() {
        switch (kind) {
            case BYTE:
                return Collections.singletonList(FieldVarType.BYTE);
            case SHORT:
                return COMP_WITH_SHORT;
            case INT:
                return COMP_WITH_INT;
            case LONG:
                return COMP_WITH_LONG;
            case FLOAT:
                return Collections.singletonList(FieldVarType.FLOAT);
            case DOUBLE:
                return COMP_WITH_DOUBLE;
            case BOOLEAN:
                return Collections.singletonList(FieldVarType.BOOLEAN);
            case CHAR:
                return Collections.singletonList(FieldVarType.CHAR);
            default:
                return Stream
                        .concat(types.stream(), Stream.of(this))
                        .filter(t -> clazz.isAssignableFrom(t.clazz))
                        .collect(Collectors.toList());
        }
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Getters / setters

    public CtClass getClazzType() {
        return clazzType;
    }

    public BitSet[] getRestrictions() {
        return restrictions;
    }

    public boolean isRestricted() {
        return restrictions != null;
    }

    // endregion
    //-------------------------------------------------------------------------
}
