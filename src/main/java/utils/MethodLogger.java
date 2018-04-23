package utils;

import javassist.CtClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

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

    public CtClass[] getCtParamsTypes() {
        return Arrays.stream(paramTypes).map(x -> x.getClazzType()).toArray(CtClass[]::new);
    }

    public FieldVarType getReturnType() {
        return returnType;
    }

    public boolean isVoid() {
        return returnType == FieldVarType.Void;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + paramTypes.hashCode();
    }
}
