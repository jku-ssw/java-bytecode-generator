package at.jku.ssw.java.bytecode.generator.metamodel.expressions.constants;

import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

/**
 * Primitive {@code boolean} type constant.
 */
public final class BooleanConstant implements Constant<Boolean> {
    private final boolean b;

    public BooleanConstant(boolean b) {
        this.b = b;
    }

    @Override
    public Boolean value() {
        return b;
    }

    @Override
    public PrimitiveType<Boolean> type() {
        return PrimitiveType.BOOLEAN;
    }

    public boolean raw() {
        return b;
    }
}
