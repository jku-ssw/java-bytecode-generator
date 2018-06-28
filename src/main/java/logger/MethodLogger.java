package logger;

import javassist.CtClass;
import utils.FieldVarType;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MethodLogger extends MyLogger {

    private String name;
    private int modifiers;
    private FieldVarType[] paramTypes;
    private FieldVarType returnType;

    private Set<MethodLogger> methodsExcludedForCalling;
    private Set<MethodLogger> calledByThisMethod;

    public MethodLogger(String name, int modifiers, FieldVarType returnType, FieldVarType... paramTypes) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.variables = new HashMap<>();
        this.paramTypes = paramTypes;
        this.methodsExcludedForCalling = new HashSet<>();
        this.calledByThisMethod = new HashSet<>();
    }

    public void addToExcludedForCalling(Set<MethodLogger> excludedForCalling) {
        methodsExcludedForCalling.addAll(excludedForCalling);
    }

    public void addMethodToCalledByThisMethod(Set<MethodLogger> calledByThisMethod) {
        this.calledByThisMethod.addAll(calledByThisMethod);
    }


    public Set<MethodLogger> getMethodsExcludedForCalling() {
        return new HashSet<>(methodsExcludedForCalling);
    }

    public Set<MethodLogger> getMethodsCalledByThisMethod() {
        return new HashSet<>(calledByThisMethod);
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
