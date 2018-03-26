package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClazzLogger {

    private Map<FieldType, Map<String, Field>> globals;
    private Map<String, MethodLogger> methods;

    public ClazzLogger() {
        methods = new HashMap<>();
        globals = new HashMap<>();
    }

    public void logMethod(MethodLogger ml) {
        methods.put(ml.getName(), ml);
    }

    public void logGlobalField(String name, FieldType type, int modifiers) {
        Field f = new Field(name, modifiers, type);
        if (globals.get(type) == null) {
            Map<String, Field> m = new HashMap<>();
            m.put(name, f);
            globals.put(type, m);
        } else {
            globals.get(type).put(name, f);
        }
    }

    public void logVariable(String name, FieldType type, String method) {
        MethodLogger ml = methods.get(method);
        Field f = new Field(name, 0, type);
        if (ml.getLocals().get(type) == null) {
            Map<String, Field> m = new HashMap<>();
            m.put(name, f);
            ml.getLocals().put(type, m);
        } else {
            ml.getLocals().get(type).put(name, f);
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
        Map<FieldType, Map<String, Field>> locals = methods.get(methodName).getLocals();
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
        for (FieldType type : FieldType.values()) {
            if (this.globals.get(type) != null && this.globals.get(type).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    public Field getField(String fieldName) {
        for (FieldType type : FieldType.values()) {
            if (this.globals.get(type) != null && this.globals.get(type).get(fieldName) != null) {
                return this.globals.get(type).get(fieldName);
            }
        }
        return null;
    }

    public boolean hasVariable(String fieldName, String methodName) {
        if (this.methods.get(methodName) != null) {
            return this.methods.get(methodName).hasVariable(fieldName);
        } else {
            return false;
        }
    }

    public Field getVariable(String fieldName, String methodName) {
        if (this.methods.get(methodName) != null) {
            return this.methods.get(methodName).getVariable(fieldName);
        } else {
            return null;
        }
    }

}
