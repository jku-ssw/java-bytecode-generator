package utils;

import java.util.*;
import java.util.stream.Collectors;

abstract class MyLogger {
    Map<String, FieldVarLogger> variables;
    static Random random = new Random();

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
        variables.put(name, f);
    }

    /**
     * @return all logged Variables
     */
    public List<FieldVarLogger> getVariables() {
        return variables.values().stream().collect(Collectors.toList());
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    /**
     * @return @code{true} if there are logged Variables
     */
    public boolean hasVariables() {
        return !variables.isEmpty();
    }

    /**
     * @return the FieldVarLogger of random logged Variable
     */
    public FieldVarLogger getVariable() {
        if (hasVariables()) {
            List<String> keys = new ArrayList<>(variables.keySet());
            return keys.size() > 0 ? variables.get(keys.get(random.nextInt(keys.size()))) : null;
        } else return null; //no variables available
    }

    public FieldVarLogger getStaticNonFinalVariable() {
        List<FieldVarLogger> staticNonFinalVariables = variables.values().stream().filter(
                v -> v.isFinal() && v.isStatic()).collect(Collectors.toList());
        if (staticNonFinalVariables.isEmpty()) return null;
        else return staticNonFinalVariables.get(random.nextInt(staticNonFinalVariables.size()));
    }

    public FieldVarLogger getNonFinalVariable() {
        List<FieldVarLogger> nonFinalVariables = variables.values().stream().filter(
                v -> v.isFinal()).collect(Collectors.toList());
        if (nonFinalVariables.isEmpty()) return null;
        return nonFinalVariables.get(random.nextInt(nonFinalVariables.size()));
    }

    public FieldVarLogger getInitializedVariable() {
        List<FieldVarLogger> initialized_variables = variables.values().stream().filter(
                v -> v.isInitialized()).collect(Collectors.toList());
        if(initialized_variables.isEmpty()) return null;
        return initialized_variables.get(random.nextInt(initialized_variables.size()));
    }

    public FieldVarLogger getCompatibleStaticInitializedVariable(FieldVarType type) {
        FieldVarType randomType = getCompatibleType(type);
        List<FieldVarLogger> compatible_variables = variables.values().stream().filter(
                v -> v.getType() == randomType && v.isStatic() && v.isInitialized()).collect(Collectors.toList());
        if(compatible_variables.isEmpty()) return null;
        return compatible_variables.get(random.nextInt(compatible_variables.size()));
    }

    public FieldVarLogger getCompatibleInitializedVariable(FieldVarType type) {
        FieldVarType randomType = getCompatibleType(type);
        List<FieldVarLogger> compatible_variables = variables.values().stream().filter(
                v -> v.getType() == randomType && v.isInitialized()).collect(Collectors.toList());
        if(compatible_variables.isEmpty()) return null;
        return compatible_variables.get(random.nextInt(compatible_variables.size()));
    }

    /**
     * @param type the type of the Variable
     * @return the FieldVarLogger of random logged Variable of this type
     */
    public FieldVarLogger getVariableOfType(FieldVarType type) {
        if (!hasVariables()) return null;
        List<FieldVarLogger> onetype_variables = variables.values().stream().filter(
                v -> v.getType() == type).collect(Collectors.toList());
        if(onetype_variables.isEmpty()) return null;
        return onetype_variables.get(random.nextInt(onetype_variables.size()));
    }

    /**
     * @param type the Type of which a compatible Field is returned
     * @return returns a random Field, that is compatible to the given Type
     */
    public FieldVarLogger getRandomCompatibleVariable(FieldVarType type) {
        FieldVarType randomType = getCompatibleType(type);
        return this.getVariableOfType(randomType);
    }


    /**
     * @param type
     * @return a random FieldVarType that is compatible to type
     */
    static FieldVarType getCompatibleType(FieldVarType type) {
        FieldVarType randomType = null;
        int i;
        switch (type) {
            case Byte:
                randomType = FieldVarType.Byte;
                break;
            case Short:
                i = random.nextInt(FieldVarType.getCompWithShort().size());
                randomType = FieldVarType.getCompWithShort().get(i);
                break;
            case Int:
                i = random.nextInt(FieldVarType.getCompWithInt().size());
                randomType = FieldVarType.getCompWithInt().get(i);
                break;
            case Long:
                i = random.nextInt(FieldVarType.getCompWithLong().size());
                randomType = FieldVarType.getCompWithLong().get(i);
                break;
            case Float:
                randomType = FieldVarType.Float;
                break;
            case Double:
                i = random.nextInt(FieldVarType.getCompWithDouble().size());
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

    /**
     * @param name the name of the variable
     * @return the FieldVarLogger of the variable with given name, that is logged in this Logger
     */
    public FieldVarLogger getVariable(String name) {
        return variables.get(name);
    }
}
