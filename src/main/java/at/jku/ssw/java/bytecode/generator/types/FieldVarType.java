package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClassUtils;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.FieldVarType.Kind.ARRAY;
import static at.jku.ssw.java.bytecode.generator.types.FieldVarType.Kind.INSTANCE;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Assignments.pAssign;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;

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
    static final Set<FieldVarType<?>> types = new HashSet<>();

    static <T> FieldVarType<T> register(FieldVarType<T> type) {
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
    public static final FieldVarType<String> STRING = register(refTypeOf(String.class));
    @SuppressWarnings("unused")
    public static final FieldVarType<Date> DATE = register(refTypeOf(Date.class));
    @SuppressWarnings("unused")
    public static final FieldVarType<Object> OBJECT = register(refTypeOf(Object.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Byte> BYTE_BOXED = register(of(Byte.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Short> SHORT_BOXED = register(of(Short.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Integer> INT_BOXED = register(of(Integer.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Long> LONG_BOXED = register(of(Long.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Float> FLOAT_BOXED = register(of(Float.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Double> DOUBLE_BOXED = register(of(Double.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Boolean> BOOLEAN_BOXED = register(of(Boolean.class));
//    @SuppressWarnings("unused")
//    public static final FieldVarType<Character> CHAR_BOXED = register(of(Character.class));

    public static final FieldVarType<Void> VOID = register(of(Void.class, CtClass.voidType, Kind.VOID));

    // endregion
    //-------------------------------------------------------------------------
    // region Type compatibility sets

    static final List<FieldVarType<?>> NUMERIC_TYPES = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.CHAR,
            FieldVarType.DOUBLE,
            FieldVarType.FLOAT,
            FieldVarType.INT,
            FieldVarType.LONG,
            FieldVarType.SHORT
    );

    static final List<FieldVarType<?>> COMP_WITH_SHORT = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR
    );

    static final List<FieldVarType<?>> COMP_WITH_INT = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR,
            FieldVarType.INT
    );

    static final List<FieldVarType<?>> COMP_WITH_LONG = Arrays.asList(
            FieldVarType.BYTE,
            FieldVarType.SHORT,
            FieldVarType.CHAR,
            FieldVarType.INT,
            FieldVarType.LONG
    );

    static final List<FieldVarType<?>> COMP_WITH_DOUBLE = Arrays.asList(
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

    /**
     * Determines the resulting type if the given array is accessed with
     * the given number of parameters (i.e. dimensions).
     * E.g. accessing int[][][] with 2 parameters yields a 1-dimensional
     * int-array.
     *
     * @param array   The accessed array
     * @param nParams The number of dimensions
     * @return the type that this array access results in
     */
    public static FieldVarType<?> resultingTypeOf(FieldVarLogger array, int nParams) {
        assert array != null;
        assert nParams > 0;

        Class<?> aClass = array.getType().clazz;

        // determine the return type
        // (e.g. accessing int[][][] with 2 parameters
        // yields a 1-dimensional array
        int remainingDim = array.getType().dim - nParams;

        FieldVarType<?> innerType = array.getType().inner;
        Class<?> componentType = ClassUtils.nthComponentType(nParams, array.getType().clazz)
                .orElseThrow(() ->
                        new AssertionError(String.format(
                                "Mismatching dimensions: %d for %s",
                                nParams,
                                aClass
                        )));

        return remainingDim == 0
                ? innerType
                : FieldVarType.arrayTypeOf(
                componentType,
                remainingDim,
                innerType
        );
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
    final CtClass clazzType;

    /**
     * The number of dimensions for array types (otherwise {@code 0}).
     */
    public final int dim;

    /**
     * Restrictions on access for array types (otherwise {@code null}).
     */
    final BitSet[] restrictions;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Initializes a new reference type based on the given class.
     *
     * @param clazz The class to base the reference type on
     * @param <T>   The type corresponding to the Java class type
     */
    public static <T> FieldVarType<T> refTypeOf(Class<T> clazz) {
        assert !clazz.isPrimitive();
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
    public static <T> FieldVarType<T> arrayTypeOf(Class<T> clazz, int dim, FieldVarType<?> inner, BitSet[] restrictions) {
        assert clazz.isArray();

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
     * @see #arrayTypeOf(Class, int, FieldVarType, BitSet[])
     */
    public static <T> FieldVarType<T> arrayTypeOf(Class<T> clazz, int dim, FieldVarType<?> inner) {
        return arrayTypeOf(clazz, dim, inner, null);
    }

    /**
     * @see #arrayTypeOf(Class, int, FieldVarType, BitSet[])
     */
    public static <T> FieldVarType<T> arrayTypeOf(Class<T> clazz, FieldVarType<?> inner) {
        return arrayTypeOf(clazz, ClassUtils.dimensions(clazz), inner, null);
    }

    /**
     * Creates an array type with the given {@link FieldVarType} describing the
     * component type and the given number of dimensions.
     *
     * @param type         The component type
     * @param dim          The number of dimensions
     * @param restrictions Optional access restrictions
     * @return an array type with the given component type, dimensions and
     * restrictions
     */
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

        return arrayTypeOf(clazz, dim, type, restrictions);
    }

    /**
     * {@link #arrayTypeOf(FieldVarType, int, BitSet[])}
     */
    public static FieldVarType<?> arrayTypeOf(FieldVarType<?> type, int dim) {
        return FieldVarType.arrayTypeOf(type, dim, null);
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
        assert !clazz.isArray();
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

    public final String inspect() {
        return String.format(
                "FieldVarType{clazz=\"%s\", kind=\"%s\", inner=\"%s\", dim=%d}",
                clazz,
                kind,
                inner,
                dim
        );
    }

    /**
     * Returns a hash that corresponds to this values.
     *
     * @param variable The variable holding this value
     * @return a hash code that identifies this value
     */
    public final String hashValue(FieldVarLogger variable) {
        String name = variable.access();

        switch (kind) {
            case BOOLEAN:
                return ternary(name, 1, 0);
            case INSTANCE:
                if (clazz.equals(String.class)) {
                    return ternary(
                            notNull(name),
                            method(name, "hashCode"),
                            "0L"
                    );
                } else if (clazz.equals(Date.class)) {
                    return ternary(
                            notNull(name),
                            method(name, "getTime"),
                            "0L"
                    );
                } else {
                    // skip other instance types for now
                    return "0L";
                }
            case ARRAY:
                return ternary(
                        notNull(name),
                        cast(field(name, "length")).to(long.class),
                        "0L"
                );
            default:
                return inPar(pAssign("(long) " + name).to("hashValue"));
        }
    }

    /**
     * Checks whether this type is assignable from the given type -
     * e.g. {@code Object} being assignable from anything
     *
     * @param other The other type whose compatibility is checked
     * @return {@code true} if the type is assignable to this type;
     * {@code false} otherwise
     */
    public boolean isAssignableFrom(FieldVarType<?> other) {
        // void is neither assignable from nor to
        if (this.kind == Kind.VOID || other.kind == Kind.VOID)
            return false;

        switch (kind) {
            case INSTANCE:
                return (other.kind == INSTANCE || other.kind == ARRAY) && clazz.isAssignableFrom(other.clazz);
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
            case ARRAY:
                return this.equals(other);
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
                        .filter(t -> t.kind != Kind.VOID)
                        .filter(t -> t.kind == Kind.ARRAY || t.kind == INSTANCE)
                        .filter(t -> clazz.isAssignableFrom(t.clazz))
                        .collect(Collectors.toList());
        }
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Getters / setters

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
