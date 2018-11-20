package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.types.specializations.RestrictedIntType;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;
import javassist.CtClass;

import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.inPar;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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

    // endregion
    //-------------------------------------------------------------------------
    // region Type accessors

    /**
     * Returns all primitive numeric types.
     *
     * @return a list of all available primitive numeric types
     */
    public static List<PrimitiveType<?>> numeric() {
        return asList(
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
    protected PrimitiveType(Class<T> clazz, CtClass clazzType, Kind kind) {
        super(clazz, clazzType, kind);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    @Override
    public final String getHashCode(FieldVarLogger<T> variable) {
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
                if (other.kind == Kind.INT || other.kind == Kind.RINT)
                    return true;
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
    public List<? extends PrimitiveType<?>> getAssignableTypes() {
        switch (kind) {
            case BYTE:
                return singletonList(BYTE);
            case SHORT:
                return asList(BYTE, SHORT, CHAR);
            case INT:
                return asList(
                        BYTE,
                        SHORT,
                        CHAR,
                        INT,
                        RestrictedIntType.INT
                );
            case LONG:
                return asList(BYTE,
                        SHORT,
                        CHAR,
                        RestrictedIntType.INT,
                        INT,
                        LONG
                );
            case FLOAT:
                return singletonList(FLOAT);
            case DOUBLE:
                return asList(FLOAT, DOUBLE);
            case BOOLEAN:
                return singletonList(BOOLEAN);
            case CHAR:
                return singletonList(CHAR);
        }

        throw ErrorUtils.shouldNotReachHere("Unexpected primitive type " + this);
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
    // region Builder methods

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<T>> builders() {
        PrimitiveType<T> self = this;

        return singletonList(
                new Builder<T>() {
                    @Override
                    public List<PrimitiveType<?>> requires() {
                        return singletonList(self);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Expression<T> build(List<Expression<?>> params) {
                        assert params.size() == 1;
                        // the builder directly defers the given expression
                        return (Expression<T>) params.get(0);
                    }

                    @Override
                    public MetaType<T> returns() {
                        return self;
                    }
                }
        );
    }

    // endregion
    //-------------------------------------------------------------------------
}
