package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClassUtils;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.types.FieldVarType.Kind.ARRAY;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.field;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

public final class ArrayType<T> extends RefType<T> {

    //-------------------------------------------------------------------------
    // region constants

    public static final int MIN_ARRAY_DIM_LENGTH = 10;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Initializes an array type.
     *
     * @param clazz        The array type descriptor
     *                     (e.g. an instance of {@code Class<int[]>})
     * @param dim          The number of dimensions of the array type
     * @param inner        The inner field type (e.g. {@link PrimitiveType#INT})
     * @param restrictions Optional restrictions on the access range
     *                     (e.g. only access dimension 0 at positions 3 to 5)
     */
    public static <T> ArrayType<T> of(Class<T> clazz, int dim, FieldVarType<?> inner, BitSet[] restrictions) {
        assert clazz.isArray();

        return new ArrayType<>(
                clazz,
                JavassistUtils.toCtClass(clazz),
                inner,
                dim,
                restrictions
        );
    }

    /**
     * @see #of(Class, int, FieldVarType, BitSet[])
     */
    public static <T> ArrayType<T> of(Class<T> clazz, int dim, FieldVarType<?> inner) {
        return of(clazz, dim, inner, null);
    }

    /**
     * @see #of(Class, int, FieldVarType, BitSet[])
     */
    public static <T> ArrayType<T> of(Class<T> clazz, FieldVarType<?> inner) {
        return of(clazz, ClassUtils.dimensions(clazz), inner, null);
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
    public static ArrayType<?> of(FieldVarType<?> type, int dim, BitSet[] restrictions) {
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

    /**
     * {@link #of(FieldVarType, int, BitSet[])}
     */
    public static ArrayType<?> of(FieldVarType<?> type, int dim) {
        return of(type, dim, null);
    }

    /**
     * Generates a new array type based on the given properties.
     *
     * @param clazz        The actual Java class type instance corresponding to
     *                     this {@link FieldVarType}.
     * @param clazzType    The {@link CtClass} type that maps to this type
     * @param inner        Optional inner type reference for array types
     * @param dim          Optional number of dimensions for array types
     * @param restrictions Optional access restrictions for array types.
     *                     Those can be specified for each dimension
     */
    private ArrayType(Class<T> clazz,
                      CtClass clazzType,
                      FieldVarType<?> inner,
                      int dim,
                      BitSet[] restrictions) {

        super(clazz, clazzType, ARRAY, inner, dim, restrictions);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Utility methods

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
                : of(
                componentType,
                remainingDim,
                innerType
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Overridden methods

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getHashCode(FieldVarLogger variable) {
        String name = variable.access();

        return ternary(
                notNull(name),
                cast(field(name, "length")).to(long.class),
                "0L"
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(FieldVarType<?> other) {
        // void is neither assignable from nor to
        return this.equals(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ArrayType<?>> getAssignableTypes() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean isArray() {
        return true;
    }

    // endregion
    //-------------------------------------------------------------------------
}
