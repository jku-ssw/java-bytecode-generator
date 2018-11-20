package at.jku.ssw.java.bytecode.generator.metamodel.base.constants;

import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

/**
 * Primitive {@code int} type constant.
 */
public final class IntConstant implements Constant<Integer> {
    private final int i;

    public IntConstant(int i) {
        this.i = i;
    }

    @Override
    public Integer value() {
        return i;
    }

    @Override
    public PrimitiveType<Integer> type() {
        return PrimitiveType.INT;
    }

    public int raw() {
        return i;
    }
}
