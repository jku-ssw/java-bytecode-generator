package at.jku.ssw.java.bytecode.generator.metamodel.base.constants;

import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;

/**
 * Primitive {@code byte} type constant.
 */
public final class ByteConstant implements Constant<Byte> {
    private final byte b;

    public ByteConstant(byte b) {
        this.b = b;
    }

    @Override
    public Byte value() {
        return b;
    }

    @Override
    public PrimitiveType<Byte> type() {
        return PrimitiveType.BYTE;
    }

    public byte raw() {
        return b;
    }
}
