package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.MetaType;

import java.lang.reflect.Modifier;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.variable;

public class FieldVarLogger {
    public final String name;
    private final int modifiers;
    private final MetaType<?> type;
    private boolean initialized;
    public final boolean isField;
    public final String clazz;

    public FieldVarLogger(String name, String clazz, int modifiers, MetaType<?> type, boolean initialized, boolean isField) {
        this.type = type;
        this.name = name;
        this.modifiers = modifiers;
        this.initialized = initialized;
        this.isField = isField;
        this.clazz = clazz;
    }

    public String access() {
        return variable(getCaller(), name);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }

    public int getModifiers() {
        return modifiers;
    }

    public MetaType<?> getType() {
        return type;
    }

    public boolean isFinal() {
        return Modifier.isFinal(modifiers);
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    /**
     * Returns the descriptor of the caller of this field or variable
     * in the corresponding class.
     *
     * @return a string representation of the caller
     */
    public String getCaller() {
        return isStatic()
                ? clazz
                : isField ? "this" : "";
    }

    @Override
    public String toString() {
        return access();
    }
}
