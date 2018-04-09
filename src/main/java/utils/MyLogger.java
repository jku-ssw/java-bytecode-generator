package utils;

import java.util.*;

abstract class MyLogger {
    Map<FieldVarType, Map<String, FieldVarLogger>> variables;

    /**
     * logs information about a generated Variable
     *
     * @param name        the name of the Variable
     * @param type        the type of the Variable
     * @param modifiers   the modifiers of the Variable
     * @param initialized @code{true} if the variable is initialized, otherwise @code{false}
     */
    public void logVariable(String name, FieldVarType type, int modifiers, boolean initialized) {
        FieldVarLogger f = new FieldVarLogger(name, modifiers, type, initialized);
        if (variables.get(type) == null) {
            Map<String, FieldVarLogger> m = new HashMap<>();
            m.put(name, f);
            variables.put(type, m);
        } else {
            variables.get(type).put(name, f);
        }
    }

    /**
     * @return all logged Variables
     */
    public List<FieldVarLogger> getVariables() {
        List<FieldVarLogger> allVariables = new ArrayList<>();
        for (Map<String, FieldVarLogger> m : variables.values()) {
            allVariables.addAll(m.values());
        }
        return allVariables;
    }

    /**
     * @param varName the name of the Variable
     * @return @code{true} if the logger contains information about this Variable, otherwise @code{false}
     */
    public boolean hasVariable(String varName) {
        for (FieldVarType type : FieldVarType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(varName) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param varName the name of the Variable
     * @return the FieldVarLogger of this Variable
     */
    public FieldVarLogger getVariable(String varName) {
        for (FieldVarType type : FieldVarType.values()) {
            if (this.variables.get(type) != null && this.variables.get(type).get(varName) != null) {
                return this.variables.get(type).get(varName);
            }
        }
        return null;
    }

    /**
     * @return @code{true} if there are logged Variables
     */
    public boolean hasVariables() {
        for (FieldVarType t : variables.keySet()) {
            if (variables.get(t) != null) return true;
        }
        return false;
    }

    /**
     * @return @code{true} if there are logged Variables of this type
     */
    public boolean hasVariables(FieldVarType type) {
        if (variables.get(type) != null) return true;
        return false;
    }

    /**
     * @return the FieldVarLogger of random logged Variable
     */
    FieldVarLogger getRandomVariable() {
        if (hasVariables()) {
            Random rnd = new Random();
            List<FieldVarType> types = new ArrayList<>(variables.keySet());
            Map<String, FieldVarLogger> oneTypeGlobals = variables.get(types.get(rnd.nextInt(types.size())));
            List<String> keys = new ArrayList<>(oneTypeGlobals.keySet());
            return oneTypeGlobals.get(keys.get(rnd.nextInt(keys.size())));
        } else {
            //TODO: maybe add to Logger
            //System.out.println("Cannot return random Variable: no Variables available");
            return null;
        }
    }

    /**
     * @param type the type of the Variable
     * @return the FieldVarLogger of random logged Variable of this type
     */
    public FieldVarLogger getRandomVariableOfType(FieldVarType type) {
        if (hasVariables(type)) {
            Random rnd = new Random();
            Map<String, FieldVarLogger> oneTypeGlobals = variables.get(type);
            List<String> keys = new ArrayList<>(oneTypeGlobals.keySet());
            return oneTypeGlobals.get(keys.get(rnd.nextInt(keys.size())));
        } else {
            //TODO: maybe add to Logger
            //System.out.println("Cannot return random Variable: no Variables available");
            return null;
        }

    }

    /**
     * @param type
     * @return a random FieldVarType that is compatible to type
     */
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
