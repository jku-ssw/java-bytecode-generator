package utils;

import java.lang.reflect.Modifier;

public class FieldVarContainer {
    private final String name;
    private final int modifiers;
    private final FieldVarType type;
    boolean initilized = false;

    public FieldVarContainer(String name, int modifiers, FieldVarType type, boolean initilized) {
        this.type = type;
        this.name = name;
        this.modifiers = modifiers;
        this.initilized = initilized;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isInitialized() {
        return initilized;
    }

    public void setInitialized() {
        this.initilized = true;
    }

    public FieldVarType getType() {
        return type;
    }

    public boolean isFinal() {
        return (modifiers & Modifier.FINAL) != 0;
    }
}
