package at.jku.ssw.java.bytecode.generator.metamodel.base.constants;

import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

/**
 * Primitive {@code float} type constant.
 */
public final class FloatConstant implements Constant<Float> {
    private final float f;

    public FloatConstant(float f) {
        this.f = f;
    }

    @Override
    public Float value() {
        return f;
    }

    @Override
    public PrimitiveType<Float> type() {
        return PrimitiveType.FLOAT;
    }

    public float raw() {
        return f;
    }
}
