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
public class PrimitiveType<T> implements MetaType<T> {

    //-------------------------------------------------------------------------
    // region Type constants

    /**
     * {@code byte} type constant.
     */
    public static final PrimitiveType<Byte> BYTE = new PrimitiveType<>(byte.class, CtClass.byteType, Kind.BYTE);

    /**
     * {@code short} type constant.
     */
    public static final PrimitiveType<Short> SHORT = new PrimitiveType<>(short.class, CtClass.shortType, Kind.SHORT);

    /**
     * {@code int} type constant.
     */
    public static final PrimitiveType<Integer> INT = new PrimitiveType<>(int.class, CtClass.intType, Kind.INT);

    /**
     * {@code long} type constant.
     */
    public static final PrimitiveType<Long> LONG = new PrimitiveType<>(long.class, CtClass.longType, Kind.LONG);

    /**
     * {@code float} type constant.
     */
    public static final PrimitiveType<Float> FLOAT = new PrimitiveType<>(float.class, CtClass.floatType, Kind.FLOAT);

    /**
     * {@code double} type constant.
     */
    public static final PrimitiveType<Double> DOUBLE = new PrimitiveType<>(double.class, CtClass.doubleType, Kind.DOUBLE);

    /**
     * {@code boolean} type constant.
     */
    public static final PrimitiveType<Boolean> BOOLEAN = new PrimitiveType<>(boolean.class, CtClass.booleanType, Kind.BOOLEAN);

    /**
     * {@code char} type constant.
     */
    public static final PrimitiveType<Character> CHAR = new PrimitiveType<>(char.class, CtClass.charType, Kind.CHAR);

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
    // region Properties

    /**
     * The described primitive type.
     */
    private final Class<T> clazz;

    /**
     * The Javassist equivalent to {@link #clazz}
     */
    private final CtClass javassistClass;

    /**
     * The primitive type kind.
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
     * Generates a new primitive type based on the given properties.
     *
     * @param clazz     The actual primitive Java class type instance
     * @param clazzType The {@link CtClass} type that maps to this type
     * @param kind      The kind descriptor to categorize different primitive
     *                  types
     */
    protected PrimitiveType(Class<T> clazz, CtClass clazzType, Kind kind) {
        assert clazz != null;
        assert clazzType != null;
        assert kind != null;
        assert clazz.isPrimitive();

        this.clazz = clazz;
        this.javassistClass = clazzType;
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
        return equals((PrimitiveType<?>) o);
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
    public final String getHashCode(FieldVarLogger<T> variable) {
        String name = variable.access();

        return kind() == Kind.BOOLEAN
                ? ternary(name, 1, 0)
                : inPar(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        // void is neither assignable from nor to anything
        if (other.kind() == Kind.VOID)
            return false;

        if (!(other instanceof PrimitiveType<?>))
            return false;

        if (kind() == other.kind())
            return true;

        switch (kind()) {
            case DOUBLE:
            case FLOAT:
                return other.kind() == Kind.FLOAT;
            case LONG:
            case INT:
                if (other.kind() == Kind.INT || other.kind() == Kind.RINT)
                    return true;
            case SHORT:
                return other.kind() == Kind.SHORT || other.kind() == Kind.CHAR || other.kind() == Kind.BYTE;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends PrimitiveType<?>> getAssignableTypes() {
        switch (kind()) {
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
