package utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * logs Information about a generated class
 */
public class ClazzLogger extends MyLogger {

    private Map<String, MethodLogger> methods;

    public ClazzLogger() {
        methods = new HashMap<>();
        this.variables = new HashMap<>();
    }

    /**
     * logs Information about a generated Method
     *
     * @param ml the MethodLogger in which the Information is stored
     */
    public void logMethod(MethodLogger ml) {
        methods.put(ml.getName(), ml);
    }

    /**
     * logs Information about a generated Variable
     *
     * @param name       the name of the Variable
     * @param type       the type of the Variable
     * @param methodName the Method in which the Variable is declared
     */
    public void logVariable(String name, FieldVarType type, String methodName, boolean initialized) {
        if (methods.get(methodName) == null) {
            System.err.println("Failed to log Variable " + name + "in Method " + methodName + ". Method does not exist");
            return;
        }
        methods.get(methodName).logVariable(name, type, 0, initialized);
    }

    /**
     * @param methodName the Method, which's local Variables are returned
     * @return a List of all FieldVarLogger-Objects of the Variables and Parameters of the Method
     */
    public List<FieldVarLogger> getLocals(String methodName) {
        MethodLogger ml = this.methods.get(methodName);
        if (ml == null) {
            System.err.println("Failed to get Locals of Method " + methodName + ": " +
                    "Method does not exist");
            return null;
        }
        return ml.getVariables();
    }

    /**
     * checks if a variable exists in a given Method
     *
     * @param fieldName  the name of the Variable
     * @param methodName the name of the Method
     * @return {@code true} if the Variable exists, else {@code false}
     */
    public boolean hasVariable(String fieldName, String methodName) {
        MethodLogger ml = methods.get(methodName);
        if (ml != null) {
            return ml.hasVariable(fieldName);
        } else return false;
    }

    /**
     * @param varName
     * @param methodName
     * @return the FieldVarLogger-Object of the Variable
     */
    public FieldVarLogger getVariable(String varName, String methodName) {
        MethodLogger ml = methods.get(methodName);
        if (ml != null) {
            return ml.getVariable(varName);
        } else {
            System.err.println("Failed to get Variable Object of Method " + methodName + ": " +
                    "Method does not exist");
            return null;
        }
    }

    /**
     * @return a random FieldVarLogger
     */
    public FieldVarLogger getRandomField() {
        return this.getRandomVariable();
    }

    /**
     * @param methodName the name of the Method
     * @return a random variable of the given Method
     */
    public FieldVarLogger getRandomVariable(String methodName) {
        if (this.methods.get(methodName) == null) {
            System.err.println("Method " + methodName + "does not exist");
            return null;
        }
        return this.methods.get(methodName).getRandomVariable();
    }

    /**
     * @param type the Type of which a compatible Field is returned
     * @return returns a random Field, that is compatible to the given Type
     */
    public FieldVarLogger getRandomCompatibleField(FieldVarType type) {
        FieldVarType randomType = getRandomCompatibleType(type);
        return this.getRandomVariableOfType(randomType);
    }

    /**
     * @param type       the Type of which a compatible Variable is returned
     * @param methodName the Method of which a random local Variable is returned
     * @return a random Variable, that is compatible to the given Type
     */
    public FieldVarLogger getRandomCompatibleVariable(FieldVarType type, String methodName) {
        if (this.methods.get(methodName) == null) {
            System.err.println("Method " + methodName + "does not exist");
            return null;
        }
        FieldVarType randomType = getRandomCompatibleType(type);
        return this.methods.get(methodName).getRandomVariableOfType(randomType);
    }

    /**
     * @param methodName the name of the Method
     * @return {@code true} if this Method has no local Variables, otherwise {@code false}
     */
    public boolean noLocals(String methodName) {
        return methods.get(methodName).noVariables();
    }

    /**
     * @param methodName the name of the Method
     * @return {@code true} if the Method exists in the generated Class, otherwise {@code false}
     */
    public boolean hasMethod(String methodName) {
        return methods.get(methodName) != null;
    }

    /**
     * @param methodName the name of the Method
     * @return returns the MethodLogger of this Method
     */
    public MethodLogger getMethodLogger(String methodName) {
        return methods.get(methodName);
    }
}
