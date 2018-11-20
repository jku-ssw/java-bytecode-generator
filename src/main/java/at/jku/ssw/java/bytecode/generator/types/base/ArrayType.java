package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ArrayInit;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.NullBuilder;
import at.jku.ssw.java.bytecode.generator.types.specializations.RestrictedIntType;
import at.jku.ssw.java.bytecode.generator.utils.ClassUtils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind.ARRAY;
import static at.jku.ssw.java.bytecode.generator.utils.IntRange.rangeFrom;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.field;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Meta type for specifying, creating and analyzing array types.
 * Array types generally extend the {@link MetaType} model by providing access
 * to the inner type ("component type") - e.g. {@code int} for {@code int[]}.
 *
 * @param <T> The actual Java class corresponding to the array type (including
 *            dimension indicator) - e.g. {@code int[][].class}
 */
public final class ArrayType<T> extends RefType<T> {

    //-------------------------------------------------------------------------
    // region Constants

    /**
     * The minimum number of elements per array dimension.
     * This restriction is necessary to allow safe access to certain array
     * areas without risking {@link ArrayIndexOutOfBoundsException} at
     * runtime.
     */
    public static final int MIN_ARRAY_DIM_LENGTH = 10;

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * Optional inner type descriptor for array types.
     */
    final MetaType<?> inner;

    /**
     * The number of dimensions for array types (otherwise {@code 0}).
     */
    final int dim;

    /**
     * Restrictions on access for array types (otherwise {@code null}).
     */
    private final BitSet[] restrictions;

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
    public static <T> ArrayType<T> of(Class<T> clazz, int dim, MetaType<?> inner, BitSet[] restrictions) {
        assert clazz.isArray();

        return new ArrayType<>(
                clazz,
                inner,
                dim,
                restrictions
        );
    }

    /**
     * @see #of(Class, int, MetaType, BitSet[])
     */
    public static <T> ArrayType<T> of(Class<T> clazz, int dim, MetaType<?> inner) {
        return of(clazz, dim, inner, UNRESTRICTED);
    }

    /**
     * @see #of(Class, int, MetaType, BitSet[])
     */
    public static <T> ArrayType<T> of(Class<T> clazz, MetaType<?> inner) {
        return of(clazz, ClassUtils.dimensions(clazz), inner, UNRESTRICTED);
    }

    /**
     * Creates an array type with the given {@link MetaType} describing the
     * component type and the given number of dimensions.
     *
     * @param type         The component type
     * @param dim          The number of dimensions
     * @param restrictions Optional access restrictions
     * @return an array type with the given component type, dimensions and
     * restrictions
     */
    public static ArrayType<?> of(MetaType<?> type, int dim, BitSet[] restrictions) {
        assert type != null : "Array type must not be null";
        assert type.kind() != Kind.VOID : "Cannot create array of void type";
        assert dim > 0 : "Invalid array dimensions";

        final String desc;
        switch (type.kind()) {
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
                desc = "L" + type.clazz().getCanonicalName() + ";";
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
     * {@link #of(MetaType, int, BitSet[])}
     */
    public static ArrayType<?> of(MetaType<?> type, int dim) {
        return of(type, dim, UNRESTRICTED);
    }

    /**
     * Generates a new array type based on the given properties.
     *
     * @param clazz        The actual Java class type instance corresponding to
     *                     this {@link MetaType}.
     * @param inner        Optional inner type reference for array types
     * @param dim          Optional number of dimensions for array types
     * @param restrictions Optional access restrictions for array types.
     *                     Those can be specified for each dimension
     */
    private ArrayType(Class<T> clazz,
                      MetaType<?> inner,
                      int dim,
                      BitSet[] restrictions) {
        super(clazz, ARRAY);

        // iff the type does not have dimensions, it must not be an array
        // and not have an inner type
        // if restrictions are given, it must be an array
        assert dim == 0 && inner == null && kind() != Kind.ARRAY && restrictions == null ||
                dim > 0 && inner != null && kind() == Kind.ARRAY;

        // the restrictions must cover all dimensions (if any)
        // and provide empty sets for unrestricted dimensions
        assert restrictions == null || restrictions.length == dim;

        this.inner = inner;
        this.dim = dim;
        this.restrictions = restrictions;
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
    public static MetaType<?> resultingTypeOf(FieldVarLogger<?> array, int nParams) {
        assert array != null;
        assert nParams > 0;

        Class<?> aClass = array.getType().clazz();

        // determine the return type
        // (e.g. accessing int[][][] with 2 parameters
        // yields a 1-dimensional array
        int remainingDim = array.getType().getDim() - nParams;

        MetaType<?> innerType = array.getType().getInner();
        Class<?> componentType = ClassUtils.nthComponentType(nParams, array.getType().clazz())
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
    public final String getHashCode(FieldVarLogger<T> variable) {
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
    public boolean isAssignableFrom(MetaType<?> other) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<?> getInner() {
        return inner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDim() {
        return dim;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BitSet[] getRestrictions() {
        return restrictions;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Builder methods

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<T>> builders() {
        ArrayType<T> self = this;

        return Arrays.asList(
                new NullBuilder<>(this),
                new Builder<T>() {
                    @Override
                    public List<? extends PrimitiveType<Integer>> requires() {
                        return Stream.generate(() ->
                                RestrictedIntType.of(
                                        rangeFrom(MIN_ARRAY_DIM_LENGTH)))
                                .limit(dim)
                                .collect(Collectors.toList());
                    }

                    @Override
                    public Expression<T> build(List<Expression<?>> params) {
                        return new ArrayInit<>(self, params);
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
