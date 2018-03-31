package utils;

import java.util.*;

abstract class MyLogger {
    Map<FieldVarType, Map<String, FieldVarContainer>> variables;


    public void logVariable(String name, FieldVarType type, int modifiers, boolean initialized) {
        FieldVarContainer f = new FieldVarContainer(name, modifiers, type, initialized);
        if (variables.get(type) == null) {
            Map<String, FieldVarContainer> m = new HashMap<>();
            m.put(name, f);
            variables.put(type, m);
        } else {
            variables.get(type).put(name, f);
        }
    }

    public List<FieldVarContainer> getVariables() {
        List<FieldVarContainer> allGlobals = new ArrayList<>();
        for (Map<String, FieldVarContainer> m : variables.values()) {
            allGlobals.addAll(m.values());
        }
        return allGlobals;
    }

    public boolean hasVariable(String fieldName) {
        for (FieldVarType type : FieldVarType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(fieldName) != null) {
                return true;
            }
        }
        return false;
    }

    public FieldVarContainer getVariable(String fieldName) {
        for (FieldVarType type : FieldVarType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(fieldName) != null) {
                return this.variables.get(type).get(fieldName);
            }
        }
        return null;
    }

    public boolean noVariables() {
        for (FieldVarType t : variables.keySet()) {
            if (variables.get(t) != null) return false;
        }
        return true;
    }

    public boolean noVariables(FieldVarType type) {
        if (variables.get(type) != null) return false;
        return true;
    }

    FieldVarContainer getRandomVariable() {
        if (noVariables()) {
            System.err.println("Cannot return random Variable: no Variables available");
            return null;
        }
        Random rnd = new Random();
        List<FieldVarType> types = new ArrayList<>(variables.keySet());
        Map<String, FieldVarContainer> oneTypeGlobals = variables.get(types.get(rnd.nextInt(types.size())));
        List<String> keys = new ArrayList<>(oneTypeGlobals.keySet());
        return oneTypeGlobals.get(keys.get(rnd.nextInt(keys.size())));
    }

    public FieldVarContainer getRandomVariableOfType(FieldVarType type) {
        if (noVariables(type)) {
            System.err.println("Cannot return random Variable: no Variables available");
            return null;
        }
        Random rnd = new Random();
        Map<String, FieldVarContainer> oneTypeGlobals = variables.get(type);
        List<String> keys = new ArrayList<>(oneTypeGlobals.keySet());
        return oneTypeGlobals.get(keys.get(rnd.nextInt(keys.size())));
    }

    static FieldVarType getRandomCompatibleType(FieldVarType type) {
        FieldVarType randomType = null;
        Random r = new Random();
        int i;
        switch (type) {
            case Byte:
                randomType = FieldVarType.Byte;
                break;
            case Short:
                i = r.nextInt(FieldVarType.getCompWithShort().size());
                randomType = FieldVarType.getCompWithShort().get(i);
                break;
            case Int:
                i = r.nextInt(FieldVarType.getCompWithInt().size());
                randomType = FieldVarType.getCompWithInt().get(i);
                break;
            case Long:
                i = r.nextInt(FieldVarType.getCompWithLong().size());
                randomType = FieldVarType.getCompWithLong().get(i);
                break;
            case Float:
                randomType = FieldVarType.Float;
                break;
            case Double:
                i = r.nextInt(FieldVarType.getCompWithDouble().size());
                randomType = FieldVarType.getCompWithDouble().get(i);
                break;
            case Boolean:
                randomType = FieldVarType.Boolean;
                break;
            case Char:
                randomType = FieldVarType.Char;
                break;
            case String:
                randomType = FieldVarType.String;
        }
        return randomType;
    }

}
