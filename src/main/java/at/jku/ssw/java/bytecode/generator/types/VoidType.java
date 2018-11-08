package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import javassist.CtClass;

import java.util.Collections;
import java.util.List;

public final class VoidType extends FieldVarType<VoidType.Void> {

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
        super(Void.class, CtClass.voidType, Kind.VOID, null, 0, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger variable) {
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
    public List<? extends FieldVarType<?>> getAssignableTypes() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignableFrom(FieldVarType<?> __) {
        return false;
    }

}
