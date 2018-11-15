package at.jku.ssw.java.bytecode.generator.metamodel.base.constants;

import at.jku.ssw.java.bytecode.generator.types.PrimitiveType;

/**
 * Primitive {@code char} type constant.
 */
public final class CharConstant implements Constant<Character> {
    private final char c;

    public CharConstant(char c) {
        this.c = c;
    }

    @Override
    public Character value() {
        return c;
    }

    @Override
    public PrimitiveType<Character> type() {
        return PrimitiveType.CHAR;
    }

    public char raw() {
        return c;
    }
}
