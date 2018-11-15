package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.MetaType.Kind.INSTANCE;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Meta type which generally describes reference types such as objects
 * of various kinds and arrays.
 *
 * @param <T> The actual Java class associated with this type
 */
public class RefType<T> extends MetaType<T> {

    //-------------------------------------------------------------------------
    // region Type constants

    /**
     * {@link Date} type constant.
     */
    public static final RefType<Date> DATE = RefType.of(Date.class);

//    @SuppressWarnings("unused")
//    public static final RefType<Byte> BYTE_BOXED = of(Byte.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Short> SHORT_BOXED = of(Short.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Integer> INT_BOXED = of(Integer.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Long> LONG_BOXED = of(Long.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Float> FLOAT_BOXED = of(Float.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Double> DOUBLE_BOXED = of(Double.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Boolean> BOOLEAN_BOXED = of(Boolean.class);
//    @SuppressWarnings("unused")
//    public static final RefType<Character> CHAR_BOXED = of(Character.class);

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Initializes a new reference type based on the given class.
     *
     * @param clazz The class to base the reference type on
     * @param <T>   The type corresponding to the Java class type
     */
    public static <T> RefType<T> of(Class<T> clazz) {
        assert !clazz.isPrimitive();
        return new RefType<>(clazz);
    }

    /**
     * Generates a new reference type based on the given properties.
     *
     * @param clazz The actual Java class type instance corresponding to
     *              this {@link MetaType}.
     */
    RefType(Class<T> clazz) {
        super(clazz, JavassistUtils.toCtClass(clazz), INSTANCE);
    }

    RefType(Class<T> clazz, CtClass clazzType, Kind kind) {
        super(clazz, clazzType, kind);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<T> variable) {
        String name = variable.access();

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
        }

        // otherwise get the hash code of the class name
        return ternary(
                notNull(name),
                method(method(method(name, "getClass"), "getSimpleName"), "hashCode"),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        return other instanceof RefType && clazz.isAssignableFrom(other.clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes()
                .filter(this::isAssignableFrom)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPrimitive() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRef() {
        return true;
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
        RefType<T> self = this;

        // TODO dynamically determine (via reflection?)!
        return Arrays.asList(
                new NullBuilder<>(this),
                new Builder.NoArgs<T>() {

                    @Override
                    public RefType<T> returns() {
                        return self;
                    }

                    @Override
                    public Expression<T> build() {
                        return new ConstructorCall<>(self);
                    }
                }
        );
    }

    // endregion
    //-------------------------------------------------------------------------
}
