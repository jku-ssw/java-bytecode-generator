package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Represents boxed versions of primitive types.
 *
 * @param <T> The actual Java class
 */
public final class BoxedType<T> extends RefType<T> {

    private final Class<?> boxed;

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

    private BoxedType(Class<T> clazz, Class<?> boxed) {
        super(clazz);
        this.boxed = boxed;
    }

    @Override
    public List<Builder<T>> builders() {
        BoxedType<T> self = this;

        return singletonList(
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
                        return new ConstructorCall<>(self, params);
                    }

                    @Override
                    public MetaType<T> returns() {
                        return self;
                    }
                }
        );
    }
}
