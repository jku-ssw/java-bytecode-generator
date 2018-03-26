package utils;

import java.lang.reflect.Modifier;

public class Field {
    private final String name;
    private final int modifiers;
    private final FieldType type;

    public Field(String name, int modifiers, FieldType type) {
        this.type = type;
        this.name = name;
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public FieldType getType() {
        return type;
    }

    public boolean isFinal() {
        return (modifiers & Modifier.FINAL) != 0;
    }
}
