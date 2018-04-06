package utils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * stores information about a Method
 */
public class MethodLogger extends MyLogger {

    private String name;

    private int modifiers;
    private Map<String, FieldVarLogger> params;

    public MethodLogger(String name) {
        this.name = name;
        this.variables = new HashMap<>();
        this.modifiers = 0;
        this.params = new HashMap<>();
    }

    /**
     * logs the modifiers of the logged method
     *
     * @param modifiers
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * logs a Parameter of the logged method
     *
     * @param paramName
     * @param type
     */
    public void logParam(String paramName, FieldVarType type) {
        FieldVarLogger fvc = new FieldVarLogger(name, modifiers, type, false);
        params.put(paramName, fvc);
    }

    /**
     * @return the name of the logged method
     */
    public String getName() {
        return name;
    }

    /**
     * @return @code{true} if the logged method is Static
     */
    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }
}
