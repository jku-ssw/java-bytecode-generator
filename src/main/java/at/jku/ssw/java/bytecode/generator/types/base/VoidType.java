package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;

import java.util.Collections;
import java.util.List;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns;

/**
 * Specialized {@link MetaType} for {@code void}.
 */
public enum VoidType implements MetaType<Void> {

    /**
     * Singleton instance.
     */
    VOID;

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
    public String getHashCode(FieldVarLogger<Void> variable) {
        throw new UnsupportedOperationException();
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
