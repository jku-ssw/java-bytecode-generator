package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;

import java.util.List;
import java.util.Objects;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Represents boxed versions of primitive types.
 *
 * @param <T> The actual Java class
 */
public class BoxedType<T> implements RefType<T> {
    //-------------------------------------------------------------------------
    // region Type constants

    /**
     * {@code byte} type constant.
     */
    public static final BoxedType<Byte> BYTE = new BoxedType<>(Byte.class, byte.class);

    /**
     * {@code short} type constant.
     */
    public static final BoxedType<Short> SHORT = new BoxedType<>(Short.class, short.class);

    /**
     * {@code int} type constant.
     */
    public static final BoxedType<Integer> INT = new BoxedType<>(Integer.class, int.class);

    /**
     * {@code long} type constant.
     */
    public static final BoxedType<Long> LONG = new BoxedType<>(Long.class, long.class);

    /**
     * {@code float} type constant.
     */
    public static final BoxedType<Float> FLOAT = new BoxedType<>(Float.class, float.class);

    /**
     * {@code double} type constant.
     */
    public static final BoxedType<Double> DOUBLE = new BoxedType<>(Double.class, double.class);

    /**
     * {@code boolean} type constant.
     */
    public static final BoxedType<Boolean> BOOLEAN = new BoxedType<>(Boolean.class, boolean.class);

    /**
     * {@code char} type constant.
     */
    public static final BoxedType<Character> CHAR = new BoxedType<>(Character.class, char.class);

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The mapped Java class.
     */
    private final Class<T> clazz;

    /**
     * The boxed primitive type.
     */
    private final Class<?> boxed;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Creates a new boxed type using the given Java class type.
     *
     * @param clazz The Java class type
     * @param boxed The boxed primitive type
     */
    private BoxedType(Class<T> clazz, Class<?> boxed) {
        assert !clazz.isArray();
        assert boxed.isPrimitive();
        this.clazz = clazz;
        this.boxed = boxed;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoxedType<?> boxedType = (BoxedType<?>) o;
        return Objects.equals(clazz, boxedType.clazz) &&
                Objects.equals(boxed, boxedType.boxed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, boxed);
    }

    @Override
    public String toString() {
        return descriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getHashCode(FieldVarLogger<T> variable) {
        String name = variable.access();

        // simply call Type#hashCode (which returns the primitive value)
        return ternary(
                notNull(name),
                method(name, "hashCode"),
                "0L"
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Builder methods

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<T>> builders() {
        return asList(
                new NullBuilder<>(this),
                new Builder<T>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public List<PrimitiveType<?>> requires() {
                        PrimitiveType<T> primitiveType =
                                (PrimitiveType<T>) TypeCache.INSTANCE.find(boxed)
                                        .orElseThrow(ErrorUtils::shouldNotReachHere);

                        return singletonList(primitiveType);
                    }

                    @Override
                    public Expression<T> build(List<Expression<?>> params) {
                        assert params.size() == 1;
                        return new ConstructorCall<>(BoxedType.this, params);
                    }

                    @Override
                    public MetaType<T> returns() {
                        return BoxedType.this;
                    }
                }
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Property accessors

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> clazz() {
        return clazz;
    }

    // endregion
    //-------------------------------------------------------------------------
}
