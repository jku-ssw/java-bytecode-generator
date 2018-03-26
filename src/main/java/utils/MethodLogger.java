package utils;

import java.util.HashMap;
import java.util.Map;

class MethodLogger {

    private String name;

    private Map<FieldType, Map<String, Field>> locals;

    //TODO maybe add field for Parameters

    public MethodLogger(String name) {
        this.name = name;
        this.locals = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<FieldType, Map<String, Field>> getLocals() {
        return locals;
    }

    public boolean hasVariable(String fieldName) {
        for (FieldType type : FieldType.values()) {
            if (this.locals.get(type) != null && this.locals.get(type).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    public Field getVariable(String fieldName) {
        for (FieldType type : FieldType.values()) {
            if (this.locals.get(type) != null) {
                return this.locals.get(type).get(fieldName);
            }
        }
        return null;
    }


}
