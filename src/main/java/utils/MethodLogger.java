package utils;

import java.util.HashMap;
import java.util.Map;

import utils.FieldType.FieldTypeName;

class MethodLogger {

    private String name;

    private Map<FieldTypeName, Map<String, Field>> locals;

    //TODO maybe add field for Parameters

    public MethodLogger(String name) {
        this.name = name;
        this.locals = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<FieldTypeName, Map<String, Field>> getLocals() {
        return locals;
    }

    public boolean hasField(String fieldName) {
        for (FieldType.FieldTypeName fieldTypeName : FieldType.FieldTypeName.values()) {
            if (this.locals.get(fieldTypeName) != null && this.locals.get(fieldTypeName).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

}
