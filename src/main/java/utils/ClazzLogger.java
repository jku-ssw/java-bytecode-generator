package utils;

import java.util.*;

/**
 * logs Information about a generated class
 */
public class ClazzLogger extends MyLogger {

    private Map<FieldVarType, Map<Integer, MethodLogger>> methods;
    private MethodLogger main;

    public ClazzLogger(MethodLogger main) {
        methods = new HashMap<>();
        variables = new HashMap<>();
        this.main = main;
    }

    /**
     * logs Information about a generated Method
     *
     * @param ml the MethodLogger in which the Information is stored
     */
    public void logMethod(MethodLogger ml) {
        Map<Integer, MethodLogger> sameReturnTypeMethods = methods.get(ml.getReturnType());
        if (sameReturnTypeMethods == null) {
            sameReturnTypeMethods = new HashMap<>();
            methods.put(ml.getReturnType(), sameReturnTypeMethods);
        }
        sameReturnTypeMethods.put(ml.hashCode(), ml);
    }

    public MethodLogger getMain() {
        return main;
    }

    /**
     * @param method the logger of the method, which's local variables are returned
     * @return a list of all FieldVarLogger-Objects of the variables and parameters of the Method
     */
    public List<FieldVarLogger> getLocals(MethodLogger method) {
        return method.getVariables();
    }

    /**
     * @return a random FieldVarLogger
     */
    public FieldVarLogger getRandomField() {
        return this.getRandomVariable();
    }

    /**
     * @param method the logger of the method
     * @return a random variable of the given Method
     */
    public FieldVarLogger getRandomVariable(MethodLogger method) {
        return method.getRandomVariable();
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
     * @param type   the Type of which a compatible Variable is returned
     * @param method the logger of the method of which a random local variable is returned
     * @return a random Variable, that is compatible to the given Type
     */
    public FieldVarLogger getRandomCompatibleVariable(FieldVarType type, MethodLogger method) {
        FieldVarType randomType = getRandomCompatibleType(type);
        return method.getRandomVariableOfType(randomType);
    }

    /**
     * @param method the logger of the method
     * @return {@code true} if this Method has no local Variables, otherwise {@code false}
     */
    public boolean hasLocals(MethodLogger method) {
        return method.hasVariables();
    }

    public MethodLogger getRandomMethod(MethodLogger callerMethod) {
        Random rnd = new Random();
        List<FieldVarType> types = new ArrayList<>(methods.keySet());
        Map<Integer, MethodLogger> sameReturnTypeMethods = methods.get(types.get(rnd.nextInt(types.size())));
        List<Integer> keys = new ArrayList<>(sameReturnTypeMethods.keySet());
        if (keys.isEmpty()) return null;
        keys.remove(Integer.valueOf(callerMethod.hashCode()));
        return keys.isEmpty() ? null : sameReturnTypeMethods.get(keys.get(rnd.nextInt(keys.size())));
    }

    public MethodLogger getRandomMethod() {
        Random rnd = new Random();
        List<FieldVarType> types = new ArrayList<>(methods.keySet());
        if (types.size() == 0) return null;
        Map<Integer, MethodLogger> sameReturnTypeMethods = methods.get(types.get(rnd.nextInt(types.size())));
        List<Integer> keys = new ArrayList<>(sameReturnTypeMethods.keySet());
        return keys.isEmpty() ? null : sameReturnTypeMethods.get(keys.get(rnd.nextInt(keys.size())));
    }

    public boolean hasMethods() {
        return !methods.isEmpty();
    }


    public MethodLogger getMethodWithReturnType(FieldVarType type) {
        Random rnd = new Random();
        Map<Integer, MethodLogger> sameReturnTypeMethods = methods.get(type);
        if (sameReturnTypeMethods == null) return null;
        List<Integer> keys = new ArrayList<>(sameReturnTypeMethods.keySet());
        return keys.isEmpty() ? null : sameReturnTypeMethods.get(keys.get(rnd.nextInt(keys.size())));
    }
}
