package at.jku.ssw.java.bytecode.generator.types.specializations;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Describes types that are dynamically registered as instances.
 *
 * @param <T> The mapped Java class
 */
public final class DynamicRefType<T> implements RefType<T> {
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The described Java {@link Class}.
     */
    private final Class<T> clazz;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Creates a new dynamic reference type.
     *
     * @param clazz The Java class to register
     */
    public DynamicRefType(Class<T> clazz) {
        assert clazz != null;
        assert !clazz.isPrimitive();
        assert !clazz.isArray();
        this.clazz = clazz;

        // TODO register in type cache
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
        return clazz == ((DynamicRefType<?>) o).clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return descriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<T> variable) {
        String name = variable.access();

        return ternary(
                notNull(name),
                method(method(method(name, "getClass"), "getSimpleName"), "hashCode"),
                "0L"
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
