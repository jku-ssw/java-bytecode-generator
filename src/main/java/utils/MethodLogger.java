package utils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class MethodLogger extends MyLogger {

    private String name;

    private int modifiers;
    private Map<String, FieldVarContainer> params;

    //TODO maybe add field for Parameters

    public MethodLogger(String name) {
        this.name = name;
        this.variables = new HashMap<>();
        this.modifiers = 0;
        this.params = new HashMap<>();
    }

    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    public void logParam(String paramName, FieldVarType type) {
        FieldVarContainer fvc = new FieldVarContainer(name, modifiers, type, false);
        params.put(paramName, fvc);
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }
}
