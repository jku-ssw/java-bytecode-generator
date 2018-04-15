package utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stores information about a Method
 */
public class MethodLogger extends MyLogger {

    private String name;
    private int modifiers;
    private FieldVarType[] paramTypes;
    private FieldVarType returnType;

    public MethodLogger(String name, int modifiers, FieldVarType returnType, FieldVarType paramTypes[]) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        variables = new HashMap<>();
        this.paramTypes = paramTypes;
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

    public FieldVarType[] getParamsTypes() {
        return paramTypes;
    }

    public FieldVarType getReturnType() {
        return returnType;
    }

}
