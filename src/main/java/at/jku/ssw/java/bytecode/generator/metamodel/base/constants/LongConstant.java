package at.jku.ssw.java.bytecode.generator.metamodel.base.constants;

import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

/**
 * Primitive {@code long} type constant.
 */
public final class LongConstant implements Constant<Long> {
    private final long l;

    public LongConstant(long l) {
        this.l = l;
    }

    @Override
    public Long value() {
        return l;
    }

    @Override
    public PrimitiveType<Long> type() {
        return PrimitiveType.LONG;
    }

    public long raw() {
        return l;
    }
}
