package utils;

public class Field {
    private String name;
    private FieldType type;
    private int modifiers;

    public Field(String name, FieldType type, int modifiers) {

        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
    }

    public String getName() {
        return name;
    }

    public FieldType getType() {
        return type;
    }

    public int getModifiers() {
        return modifiers;
    }

}
