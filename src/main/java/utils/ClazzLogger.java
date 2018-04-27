package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * logs Information about a generated class
 */
public class ClazzLogger extends MyLogger {

    private List<MethodLogger> methods;
    private MethodLogger main;

    public ClazzLogger(MethodLogger main) {
        methods = new ArrayList<>();
        variables = new HashMap<>();
        this.main = main;
    }

    /**
     * logs Information about a generated Method
     *
     * @param ml the MethodLogger in which the Information is stored
     */
    public void logMethod(MethodLogger ml) {
        methods.add(ml);
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
     * @return the MethodLogger of a randomly chosen method, that is logged in the clazzLogger
     */
    public MethodLogger getRandomMethod() {
        if (hasMethods()) return methods.get(random.nextInt(methods.size()));
        else return null;
    }


    public MethodLogger getRandomCallableMethod(MethodLogger callerMethod) {
        if (!callerMethod.isStatic()) return getRandomMethod();
        else {
            List<MethodLogger> staticMethods = getStaticMethods(callerMethod);
            if(staticMethods.isEmpty()) return null;
            return staticMethods.get(random.nextInt(staticMethods.size()));
        }
    }

    private List<MethodLogger> getStaticMethods(MethodLogger callerMethod) {
        if (hasMethods()) {
            return methods.stream().filter(
                    m -> m.isStatic() && m.hashCode() != callerMethod.hashCode()).collect(Collectors.toList());
        } else return null;
    }

    /**
     * @return @code{true} if there are logged methods in this clazzLogger, otherwise @code{false}
     */
    public boolean hasMethods() {
        return !methods.isEmpty();
    }

    /**
     * @param type the return-type of the randomly choosen method
     * @return the MethodLogger of a randomly chosen method with given return-type
     */
    public MethodLogger getMethodWithReturnType(FieldVarType type) {
        if (hasMethods()) {
            List<MethodLogger> retTypeMethods = methods.stream().filter(
                    m -> m.getReturnType() == type).collect(Collectors.toList());
            if (retTypeMethods.isEmpty()) return null;
            return retTypeMethods.get(random.nextInt(retTypeMethods.size()));
        } else return null;
    }
}
