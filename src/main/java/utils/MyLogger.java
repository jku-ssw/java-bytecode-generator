package utils;

import java.util.*;

abstract class MyLogger {
    Map<FieldType, Map<String, Field>> variables;


    public void logVariable(String name, FieldType type, int modifiers) {
        Field f = new Field(name, modifiers, type);
        if (variables.get(type) == null) {
            Map<String, Field> m = new HashMap<>();
            m.put(name, f);
            variables.put(type, m);
        } else {
            variables.get(type).put(name, f);
        }
    }

    public List<Field> getVariables() {
        List<Field> allGlobals = new ArrayList<>();
        for (Map<String, Field> m : variables.values()) {
            allGlobals.addAll(m.values());
        }
        return allGlobals;
    }

    public boolean hasVariable(String fieldName) {
        for (FieldType type : FieldType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    public Field getVariable(String fieldName) {
        for (FieldType type : FieldType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(fieldName) != null) {
                return this.variables.get(type).get(fieldName);
            }
        }
        return null;
    }

    public boolean noVariables() {
        for (FieldType t : variables.keySet()) {
            if (variables.get(t) != null) return false;
        }
        return true;
    }

    public Field getRandomVariable() {
        if (noVariables()) {
            System.err.println("Cannot return random Variable: no Variables available");
            return null;
        }
        Random rnd = new Random();
        List<FieldType> types = new ArrayList<>(variables.keySet());
        Map<String, Field> oneTypeGlobals = variables.get(types.get(rnd.nextInt(types.size())));
        List<String> keys = new ArrayList<>(oneTypeGlobals.keySet());
        return oneTypeGlobals.get(keys.get(rnd.nextInt(keys.size())));
    }

}
