package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import javassist.CtClass;

import java.util.Collections;
import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns;

/**
 * Specialized {@link MetaType} for {@code void}.
 */
public final class VoidType implements MetaType<Void> {

    /**
     * Singleton instance.
     */
    public static final VoidType VOID = new VoidType();

    /**
     * Creates the {@code void} type.
     */
    private VoidType() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equals((VoidType) o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Patterns.VOID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Kind kind() {
        return Kind.VOID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Void> clazz() {
        return Void.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CtClass javassistClazz() {
        return CtClass.voidType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<Void> variable) {
        throw new UnsupportedOperationException();
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
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends MetaType<?>> getAssignableTypes() {
        // void is incompatible with everything
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> __) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Builder<Void>> builders() {
        // Void should not be built
        return Collections.emptyList();
    }

}
