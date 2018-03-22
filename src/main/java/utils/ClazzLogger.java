package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClazzLogger {
    private Map<FieldType, List<Field>> globals;
    private Map<String, MethodLogger> methods;

    public ClazzLogger() {
        methods = new HashMap<>();
        globals = new HashMap<>();
    }

    public void putMethod(MethodLogger ml) {
        methods.put(ml.getName(), ml);
    }

    public void logGlobalField(String name, FieldType type, int modifiers) {
        Field f = new Field(name, type, modifiers);
        if (globals.get(type) == null) {
            List<Field> l = new ArrayList<>();
            l.add(f);
            globals.put(type, l);
        } else {
            globals.get(type).add(f);
        }
    }

    public void logLocalField(String name, FieldType type, String method) {
        MethodLogger ml = methods.get(method);
        Field f = new Field(name, type, -1);
        if (ml.getLocals().get(type) == null) {
            List<Field> l = new ArrayList<>();
            l.add(f);
            ml.getLocals().put(type, l);
        } else {
            ml.getLocals().get(type).add(f);
        }
    }

    public List<Field> getGlobals() {
        List<Field> allGlobals = new ArrayList<>();
        for (List<Field> l : globals.values()) {
            allGlobals.addAll(l);
        }
        return allGlobals;
    }

    public List<Field> getLocals(String meth) {
        Map<FieldType, List<Field>> locals = methods.get(meth).getLocals();
        List<Field> allLocals = new ArrayList<>();
        for (List<Field> l : locals.values()) {
            allLocals.addAll(l);
        }
        return allLocals;
    }

    public void logGlobalField(String name, FieldType type) {
        logGlobalField(name, type, -1);
    }
}
