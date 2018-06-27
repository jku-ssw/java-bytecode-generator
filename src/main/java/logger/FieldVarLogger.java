package logger;

import utils.FieldVarType;

import java.lang.reflect.Modifier;

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

    public String getName() {
        return name;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized() {
        this.initialized = true;
    }

    public FieldVarType getType() {
        return type;
    }

    public boolean isFinal() {
      return Modifier.isFinal(modifiers);
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
}
