package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
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

    /**
     * @param type the Type of which a compatible Field is returned
     * @return returns a random Field, that is compatible to the given Type
     */
    public FieldVarLogger getCompatibleVariable(FieldVarType type) {
        if (!hasVariables()) return null;
        List<FieldVarType> compatibleTypes = FieldVarType.getCompatibleTypes(type);
        List<FieldVarLogger> compatible_variables =
                variables.values().stream().filter(v -> compatibleTypes.stream().anyMatch(
                        r -> r == v.getType())).collect(Collectors.toList());
        if (compatible_variables.isEmpty()) return null;
        return compatible_variables.get(random.nextInt(compatible_variables.size()));
    }

    public FieldVarLogger getCompatibleVariableWithPredicate(Predicate<FieldVarLogger> predicate, FieldVarType type) {
        if (!hasVariables()) return null;
        List<FieldVarType> compatibleTypes = FieldVarType.getCompatibleTypes(type);
        List<FieldVarLogger> compatible_variables =
                variables.values().stream().filter(v -> compatibleTypes.stream().anyMatch(
                        r -> r == v.getType())).filter(predicate).collect(Collectors.toList());
        if (compatible_variables.isEmpty()) return null;
        return compatible_variables.get(random.nextInt(compatible_variables.size()));
    }

    /**
     * @param name the name of the variable
     * @return the FieldVarLogger of the variable with given name, that is logged in this Logger
     */
    public FieldVarLogger getVariable(String name) {
        return variables.get(name);
    }

    public FieldVarLogger getVariableWithPredicate(Predicate<FieldVarLogger> predicate) {
        if (!hasVariables()) return null;
        List<FieldVarLogger> oneType_variables = variables.values().stream().filter(
                predicate).collect(Collectors.toList());
        if (oneType_variables.isEmpty()) return null;
        return oneType_variables.get(random.nextInt(oneType_variables.size()));
    }


}
