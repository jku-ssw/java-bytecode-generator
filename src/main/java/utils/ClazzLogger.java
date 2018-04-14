package utils;

import java.util.*;

/**
 * logs Information about a generated class
 */
public class ClazzLogger extends MyLogger {

    private Map<FieldVarType, Map<String, MethodLogger>> methods;

    public ClazzLogger() {
        methods = new HashMap<>();
        variables = new HashMap<>();
    }

    /**
     * logs Information about a generated Method
     *
     * @param ml the MethodLogger in which the Information is stored
     */
    public void logMethod(MethodLogger ml) {
        Map<String, MethodLogger> sameReturnTypeMethods = methods.get(ml.getReturnType());
        if (sameReturnTypeMethods == null) {
            sameReturnTypeMethods = new HashMap<>();
            methods.put(ml.getReturnType(), sameReturnTypeMethods);
        }
        sameReturnTypeMethods.put(ml.getName(), ml);
    }

    /**
     * logs Information about a generated Variable
     *
     * @param name       the name of the Variable
     * @param type       the type of the Variable
     * @param methodName the Method in which the Variable is declared
     */
    public void logVariable(String name, FieldVarType type, String methodName, boolean initialized) {
        for (Map<String, MethodLogger> sameReturnTypeMethods : methods.values()) {
            if (sameReturnTypeMethods.get(methodName) != null) {
                sameReturnTypeMethods.get(methodName).logVariable(name, type, 0, initialized);
                return;
            }
        }
        System.err.println("Failed to log Variable " + name + "in Method " + methodName + ". Method does not exist");
        return;
    }

    /**
     * @param methodName the Method, which's local Variables are returned
     * @return a List of all FieldVarLogger-Objects of the Variables and Parameters of the Method
     */
    public List<FieldVarLogger> getLocals(String methodName) {
        MethodLogger ml = getMethodLogger(methodName);
        if (ml != null) {
            return ml.getVariables();
        } else {
            System.err.println("Failed to get Locals of Method " + methodName + ": " +
                    "Method does not exist");
            return null;
        }
    }

//    /**
//     * checks if a variable exists in a given Method
//     *
//     * @param fieldName  the name of the Variable
//     * @param methodName the name of the Method
//     * @return {@code true} if the Variable exists, else {@code false}
//     */
//    public boolean hasVariable(String fieldName, String methodName) {
//        MethodLogger ml = getMethodLogger(methodName);
//        if (ml != null) {
//            return ml.hasVariable(fieldName);
//        } else return false;
//    }

    /**
     * @param varName
     * @param methodName
     * @return the FieldVarLogger-Object of the Variable
     */
    public FieldVarLogger getVariable(String varName, String methodName) {
        MethodLogger ml = getMethodLogger(methodName);
        if (ml != null) {
            return ml.getVariable(varName);
        } else {
            System.err.println("Failed to get Variable of Method " + methodName + ": " +
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
        MethodLogger ml = getMethodLogger(methodName);
        if (ml != null) {
            return ml.getRandomVariable();
        } else {
            System.err.println("Failed to get random Variable: Method " + methodName + "does not exist");
            return null;
        }
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
        MethodLogger ml = getMethodLogger(methodName);
        if (ml != null) {
            FieldVarType randomType = getRandomCompatibleType(type);
            return ml.getRandomVariableOfType(randomType);
        } else {
            System.err.println("Failed to get random compatible Variable: Method " + methodName + "does not exist");
            return null;
        }
    }

    /**
     * @param methodName the name of the Method
     * @return {@code true} if this Method has no local Variables, otherwise {@code false}
     */
    public boolean hasLocals(String methodName) {
        MethodLogger ml = getMethodLogger(methodName);
        if (ml != null) return ml.hasVariables();
        else return false;
    }

    /**
     * @param methodName the name of the Method
     * @return {@code true} if the Method exists in the generated Class, otherwise {@code false}
     */
    public boolean hasMethod(String methodName) {
        return getMethodLogger(methodName) != null;
    }

    /**
     * @param methodName the name of the Method
     * @return returns the MethodLogger of this Method
     */
    public MethodLogger getMethodLogger(String methodName) {
        MethodLogger ml = null;
        for (Map<String, MethodLogger> sameReturnTypeMethods : methods.values()) {
            ml = sameReturnTypeMethods.get(methodName);
            if (ml != null) break;
        }
        return ml;
    }

    public MethodLogger getRandomMethod(String callerMethod) {
        Random rnd = new Random();
        List<FieldVarType> types = new ArrayList<>(methods.keySet());
        Map<String, MethodLogger> sameReturnTypeMethods = methods.get(types.get(rnd.nextInt(types.size())));
        List<String> keys = new ArrayList<>(sameReturnTypeMethods.keySet());
        keys.remove("main");
        keys.remove(callerMethod);
        return keys.isEmpty() ? null : sameReturnTypeMethods.get(keys.get(rnd.nextInt(keys.size())));
    }

    public boolean hasMethods() {
        return !methods.isEmpty();
    }

    //TODO
    public MethodLogger getMethodWithReturnType(FieldVarType type) {
        Random rnd = new Random();
        Map<String, MethodLogger> sameReturnTypeMethods = methods.get(type);
        if (sameReturnTypeMethods == null) return null;
        List<String> keys = new ArrayList<>(sameReturnTypeMethods.keySet());
        keys.remove("main");
        return keys.isEmpty() ? null : sameReturnTypeMethods.get(keys.get(rnd.nextInt(keys.size())));
    }
}
