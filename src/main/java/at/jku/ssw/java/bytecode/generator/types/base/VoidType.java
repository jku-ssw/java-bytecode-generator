package at.jku.ssw.java.bytecode.generator.types.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import javassist.CtClass;

import java.util.Collections;
import java.util.List;

/**
 * Specialized {@link MetaType} for {@code void}.
 */
public final class VoidType extends MetaType<VoidType.Void> {

    /**
     * Singleton instance.
     */
    public static final VoidType VOID = new VoidType();

    /**
     * Dummy type.
     */
    static final class Void {
        /* empty */
    }

    /**
     * Creates the {@code void} type.
     */
    private VoidType() {
        super(Void.class, CtClass.voidType, Kind.VOID);
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
