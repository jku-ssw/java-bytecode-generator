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
    public void logVariable(String name, FieldType type, String methodName) {
        if (methods.get(methodName) == null) {
            System.err.println("Failed to log Variable " + name + "in Method " + methodName + ". Method does not exist");
            return;
        }
        methods.get(methodName).logVariable(name, type, 0);
    }

    /**
     * @param methodName the Method, which's local Variables are returned
     * @return returns a List of all Field-Objects of the Variables and Paramters of the method
     */
    public List<Field> getLocals(String methodName) {
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
     * @param fieldName  the name of the variable
     * @param methodName the name of the method
     * @return {@code true} if the variable exists, else {@code false}
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
     * @return returns the Field-Object of the variable
     */
    public Field getVariable(String varName, String methodName) {
        MethodLogger ml = methods.get(methodName);
        if (ml != null) {
            return ml.getVariable(varName);
        } else {
            System.err.println("Failed to get Variable Object of Method " + methodName + ": " +
                    "Method does not exist");
            return null;
        }
    }

    public Field getRandomVariable(String methodName) {
        if (this.methods.get(methodName) == null) {
            System.err.println("Method " + methodName + "does not exist");
            return null;
        }
        return this.methods.get(methodName).getRandomVariable();
    }

    public Field getRandomVariableOfType(FieldType type, String methodName) {
        if (this.methods.get(methodName) == null) {
            System.err.println("Method " + methodName + "does not exist");
            return null;
        }
        return this.methods.get(methodName).getRandomVariableOfType(type);
    }

    public boolean noLocals(String methodName) {
        return methods.get(methodName).noVariables();
    }
}
