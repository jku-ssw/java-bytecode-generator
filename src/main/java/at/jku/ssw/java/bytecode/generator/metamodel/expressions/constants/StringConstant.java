package at.jku.ssw.java.bytecode.generator.metamodel.expressions.constants;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;

/**
 * A constant expression of type {@link String}.
 */
public class StringConstant implements Constant<String> {

    private final String str;

    public StringConstant(String str) {
        this.str = str;
    }

    @Override
    public String value() {
        return str;
    }

    @Override
    public MetaType<String> type() {
        return StringType.STRING;
    }
}
