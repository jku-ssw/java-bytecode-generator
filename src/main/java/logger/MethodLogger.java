package logger;

import javassist.CtClass;
import utils.FieldVarType;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * stores information about a Method
 */
public class MethodLogger extends MyLogger {

    private String name;
    private int modifiers;
    private FieldVarType[] paramTypes;
    private FieldVarType returnType;

    private List<MethodLogger> methodsExcludedForCalling;

    public MethodLogger(String name, int modifiers, FieldVarType returnType, FieldVarType... paramTypes) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.variables = new HashMap<>();
        this.paramTypes = paramTypes;
        this.methodsExcludedForCalling = new ArrayList<>();
    }

    public void addMethodToExcludedForCalling(MethodLogger callingMethod) {
        methodsExcludedForCalling.add(callingMethod);
    }

    public List<MethodLogger> getMethodsExcludedForCalling() {
        return methodsExcludedForCalling;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return (modifiers & Modifier.STATIC) != 0;
    }

    public FieldVarType[] getParamsTypes() {
        return paramTypes;
    }

    public CtClass[] getCtParamTypes() {
        if (paramTypes == null) return new CtClass[0];
        return Arrays.stream(paramTypes).map(x -> x.getClazzType()).toArray(CtClass[]::new);
    }

    public FieldVarType getReturnType() {
        return returnType;
    }

    public boolean isVoid() {
        return returnType == FieldVarType.VOID;
    }

    @Override
    public int hashCode() {
        return paramTypes == null ? name.hashCode() : name.hashCode() + paramTypes.hashCode();
    }
}
