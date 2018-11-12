package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import javassist.CtClass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.inPar;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Meta type that describes primitive types.
 *
 * @param <T> The Java primitive type class that is associated with this type
 */
public class PrimitiveType<T> extends MetaType<T> {

    //-------------------------------------------------------------------------
    // region Type constants

    /**
     * {@code byte} type constant.
     */
    public static final PrimitiveType<Byte> BYTE = of(byte.class, CtClass.byteType, Kind.BYTE);


    /**
     * {@code short} type constant.
     */
    public static final PrimitiveType<Short> SHORT = of(short.class, CtClass.shortType, Kind.SHORT);
    /**
     * {@code int} type constant.
     */
    public static final PrimitiveType<Integer> INT = of(int.class, CtClass.intType, Kind.INT);
    /**
     * {@code long} type constant.
     */
    public static final PrimitiveType<Long> LONG = of(long.class, CtClass.longType, Kind.LONG);
    /**
     * {@code float} type constant.
     */
    public static final PrimitiveType<Float> FLOAT = of(float.class, CtClass.floatType, Kind.FLOAT);
    /**
     * {@code double} type constant.
     */
    public static final PrimitiveType<Double> DOUBLE = of(double.class, CtClass.doubleType, Kind.DOUBLE);
    /**
     * {@code boolean} type constant.
     */
    public static final PrimitiveType<Boolean> BOOLEAN = of(boolean.class, CtClass.booleanType, Kind.BOOLEAN);
    /**
     * {@code char} type constant.
     */
    public static final PrimitiveType<Character> CHAR = of(char.class, CtClass.charType, Kind.CHAR);

    //-------------------------------------------------------------------------
    // region Type compatibility sets

    /**
     * Constant mapping of compatible types with {@code short}.
     * I.e. {@code short.class.isAssignableFrom(type)} is {@code true}.
     */
    private static final List<PrimitiveType<?>> COMP_WITH_SHORT = Arrays.asList(
            BYTE,
            SHORT,
            CHAR
    );

    /**
     * Constant mapping of compatible types with {@code int}.
     * I.e. {@code int.class.isAssignableFrom(type)} is {@code true}.
     */
    private static final List<PrimitiveType<?>> COMP_WITH_INT = Arrays.asList(
            BYTE,
            SHORT,
            CHAR,
            INT
    );

    /**
     * Constant mapping of compatible types with {@code long}.
     * I.e. {@code long.class.isAssignableFrom(type)} is {@code true}.
     */
    private static final List<PrimitiveType<?>> COMP_WITH_LONG = Arrays.asList(
            BYTE,
            SHORT,
            CHAR,
            INT,
            LONG
    );

    /**
     * Constant mapping of compatible types with {@code double}.
     * I.e. {@code double.class.isAssignableFrom(type)} is {@code true}.
     */
    private static final List<PrimitiveType<?>> COMP_WITH_DOUBLE = Arrays.asList(
            FLOAT,
            DOUBLE
    );

    // endregion
    //-------------------------------------------------------------------------
    // region Type accessors

    /**
     * Returns all primitive numeric types.
     *
     * @return a list of all available primitive numeric types
     */
    public static List<PrimitiveType<?>> numeric() {
        return Arrays.asList(
                BYTE,
                CHAR,
                DOUBLE,
                FLOAT,
                INT,
                LONG,
                SHORT
        );
    }


    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Initializes special types that are backed by specific {@link CtClass}
     * type constants (i.e. primitive types, void).
     *
     * @param clazz     The corresponding class (e.g. {@code int.class})
     * @param clazzType The mapped {@link CtClass} type instance
     *                  (e.g. {@link CtClass#intType}.
     * @param kind      The kind of the type
     */
    public static <T> PrimitiveType<T> of(Class<T> clazz, CtClass clazzType, Kind kind) {
        assert !clazz.isArray();
        return new PrimitiveType<>(clazz, clazzType, kind);
    }

    /**
     * Generates a new type based on the given properties.
     *
     * @param clazz     The actual Java class type instance corresponding to
     *                  this {@link MetaType}.
     * @param clazzType The {@link CtClass} type that maps to this type
     * @param kind      The kind descriptor to catgorize different types
     */
    private PrimitiveType(Class<T> clazz, CtClass clazzType, Kind kind) {
        super(clazz, clazzType, kind);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    @Override
    public final String getHashCode(FieldVarLogger variable) {
        String name = variable.access();

        return kind == Kind.BOOLEAN
                ? ternary(name, 1, 0)
                : inPar(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        // void is neither assignable from nor to anything
        if (other.kind == Kind.VOID)
            return false;

        if (!(other instanceof PrimitiveType<?>))
            return false;

        if (kind == other.kind)
            return true;

        switch (kind) {
            case DOUBLE:
            case FLOAT:
                return other.kind == Kind.FLOAT;
            case LONG:
            case INT:
                if (other.kind == Kind.INT) return true;
            case SHORT:
                return other.kind == Kind.SHORT || other.kind == Kind.CHAR || other.kind == Kind.BYTE;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PrimitiveType<?>> getAssignableTypes() {
        switch (kind) {
            case BYTE:
                return Collections.singletonList(BYTE);
            case SHORT:
                return COMP_WITH_SHORT;
            case INT:
                return COMP_WITH_INT;
            case LONG:
                return COMP_WITH_LONG;
            case FLOAT:
                return Collections.singletonList(FLOAT);
            case DOUBLE:
                return COMP_WITH_DOUBLE;
            case BOOLEAN:
                return Collections.singletonList(BOOLEAN);
            case CHAR:
                return Collections.singletonList(CHAR);
        }

        /* should not happen */
        throw new AssertionError("Unexpected primitive type " + this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRef() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isArray() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isVoid() {
        return false;
    }

    // endregion
    //-------------------------------------------------------------------------
}
