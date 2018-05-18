package logger;

import utils.FieldVarType;

import java.lang.reflect.Modifier;

/**
 * stores information about Fields and Variables
 */
public class FieldVarLogger {
    private final String name;
    private final int modifiers;
    private final FieldVarType type;
    private boolean initialized;

    public FieldVarLogger(String name, int modifiers, FieldVarType type, boolean initialized) {
        this.type = type;
        this.name = name;
        this.modifiers = modifiers;
        this.initialized = initialized;
    }

    /**
     * @return the name of this Field or Variable
     */
    public String getName() {
        return name;
    }

    /**
     * @return @code{true} if this Field or Variable is initialized, otherwise @code{false}
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * marks this Field or Variable as initialized
     */
    public void setInitialized() {
        this.initialized = true;
    }

    /**
     * @return the FieldVarType of this Field or Variable
     */
    public FieldVarType getType() {
        return type;
    }

    /**
     * @return @code{true} if this Field or Variable is final, otherwise @code{false}
     */
    public boolean isFinal() {
      return Modifier.isFinal(modifiers);
    }

    /**
     * @return @code{true} if this Field or Variable is static, otherwise @code{false}
     */
    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
}
