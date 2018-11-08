package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.FieldVarType.Kind.INSTANCE;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * TODO
 *
 * @param <T> TODO
 */
public class RefType<T> extends FieldVarType<T> {

    //-------------------------------------------------------------------------
    // region Type constants

    public static final RefType<String> STRING = RefType.of(String.class);
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
        return new RefType<>(
                clazz,
                JavassistUtils.toCtClass(clazz)
        );
    }

    /**
     * Generates a new reference type based on the given properties.
     *
     * @param clazz     The actual Java class type instance corresponding to
     *                  this {@link FieldVarType}.
     * @param clazzType The {@link CtClass} type that maps to this type
     */
    private RefType(Class<T> clazz, CtClass clazzType) {
        super(clazz, clazzType, INSTANCE, null, 0, null);
    }

    public RefType(Class<T> clazz,
                   CtClass clazzType,
                   Kind kind,
                   FieldVarType<?> inner,
                   int dim,
                   BitSet[] restrictions) {

        super(clazz, clazzType, kind, inner, dim, restrictions);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger variable) {
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
    public boolean isAssignableFrom(FieldVarType<?> other) {
        return other instanceof RefType && clazz.isAssignableFrom(other.clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends RefType<?>> getAssignableTypes() {
        return TypeCache.INSTANCE.refTypes()
                .filter(t -> clazz.isAssignableFrom(t.clazz))
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
}
