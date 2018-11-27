package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.LibMethod;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.NullBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

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
    public static final BoxedType<Byte> BYTE = new BoxedType<>(Byte.class, PrimitiveType.BYTE);

    /**
     * {@code short} type constant.
     */
    public static final BoxedType<Short> SHORT = new BoxedType<>(Short.class, PrimitiveType.SHORT);

    /**
     * {@code int} type constant.
     */
    public static final BoxedType<Integer> INT = new BoxedType<>(Integer.class, PrimitiveType.INT);

    /**
     * {@code long} type constant.
     */
    public static final BoxedType<Long> LONG = new BoxedType<>(Long.class, PrimitiveType.LONG);

    /**
     * {@code float} type constant.
     */
    public static final BoxedType<Float> FLOAT = new BoxedType<>(Float.class, PrimitiveType.FLOAT);

    /**
     * {@code double} type constant.
     */
    public static final BoxedType<Double> DOUBLE = new BoxedType<>(Double.class, PrimitiveType.DOUBLE);

    /**
     * {@code boolean} type constant.
     */
    public static final BoxedType<Boolean> BOOLEAN = new BoxedType<>(Boolean.class, PrimitiveType.BOOLEAN);

    /**
     * {@code char} type constant.
     */
    public static final BoxedType<Character> CHAR = new BoxedType<>(Character.class, PrimitiveType.CHAR);

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
    private final PrimitiveType<?> boxed;

    /**
     * The methods that are available for wrapper classes.
     */
    private final List<LibMethod<?>> methods;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Creates a new boxed type using the given Java class type.
     *
     * @param clazz The Java class type
     * @param boxed The boxed primitive type
     */
    private BoxedType(Class<T> clazz, PrimitiveType<T> boxed) {
        assert !clazz.isArray();
        assert !clazz.isPrimitive();
        this.clazz = clazz;
        this.boxed = boxed;
        this.methods = inferMethods();
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
        return descriptor().hashCode();
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
                        return singletonList(boxed);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<LibMethod<?>> methods() {
        return methods;
    }

    // endregion
    //-------------------------------------------------------------------------
}
