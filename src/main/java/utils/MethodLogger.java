package utils;

import javassist.CtClass;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;

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
     * @return @code{true} if the logged method is static, otherwise @code{false}
     */
    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    public FieldVarType[] getParamsTypes() {
        return paramTypes;
    }

    public CtClass[] getCtParamsTypes() {
        return Arrays.stream(paramTypes).map(x -> x.getClazzType()).toArray(CtClass[]::new);
    }

    /**
     * @return the return-type of this method, given by its FieldVarType
     */
    public FieldVarType getReturnType() {
        return returnType;
    }

    /**
     * @return @code{true} if the logged method is void, otherwise @code{false}
     */
    public boolean isVoid() {
        return returnType == FieldVarType.Void;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + paramTypes.hashCode();
    }
}
