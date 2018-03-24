package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.FieldType.FieldTypeName;

public class ClazzLogger {

    private Map<FieldTypeName, Map<String, Field>> globals;
    private Map<String, MethodLogger> methods;

    public ClazzLogger() {
        methods = new HashMap<>();
        globals = new HashMap<>();
    }

    public void logMethod(MethodLogger ml) {
        methods.put(ml.getName(), ml);
    }

    public void logGlobalField(String name, FieldType type, int modifiers) {
        Field f = new Field(name, modifiers);
        if (globals.get(type.getName()) == null) {
            Map<String, Field> m = new HashMap<>();
            m.put(name, f);
            globals.put(type.getName(), m);
        } else {
            globals.get(type.getName()).put(name, f);
        }
    }

    public void logLocalVariable(String name, FieldType type, String method) {
        MethodLogger ml = methods.get(method);
        Field f = new Field(name, -1);
        if (ml.getLocals().get(type) == null) {
            Map<String, Field> m = new HashMap<>();
            m.put(name, f);
            ml.getLocals().put(type.getName(), m);
        } else {
            ml.getLocals().get(type.getName()).put(name, f);
        }
    }

    public List<Field> getGlobals() {
        List<Field> allGlobals = new ArrayList<>();
        for (Map<String, Field> m : globals.values()) {
            allGlobals.addAll(m.values());
        }
        return allGlobals;
    }

    public List<Field> getLocals(String methodName) {
        Map<FieldTypeName, Map<String, Field>> locals = methods.get(methodName).getLocals();
        List<Field> allLocals = new ArrayList<>();
        for (Map<String, Field> m : locals.values()) {
            allLocals.addAll(m.values());
        }
        return allLocals;
    }

    public void logGlobalField(String name, FieldType type) {
        logGlobalField(name, type, -1);
    }

    public boolean hasField(String fieldName) {
        for (FieldTypeName fieldTypeName : FieldType.FieldTypeName.values()) {
            if (this.globals.get(fieldTypeName) != null && this.globals.get(fieldTypeName).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    public boolean hasField(String fieldName, String methodName) {
        if (this.methods.get(methodName) != null) {
            return this.methods.get(methodName).hasField(fieldName);
        } else {
            return false;
        }
    }
}
