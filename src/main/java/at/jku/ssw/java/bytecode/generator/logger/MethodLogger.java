package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.FieldVarType;
import javassist.CtClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.VoidType.VOID;

public class MethodLogger extends Logger {

    private static final String TO_STRING_FORMAT = "method %s%s %s %s(%s)";

    private final boolean inherited;
    private String name;
    private int modifiers;
    private FieldVarType[] paramTypes;
    private FieldVarType<?> returnType;

    private Set<MethodLogger> methodsExcludedForCalling;
    private Set<MethodLogger> calledByThisMethod;

    public final String clazz;

    public MethodLogger(Random rand, String clazz, String name, int modifiers, FieldVarType<?> returnType, FieldVarType... paramTypes) {
        this(rand, clazz, name, modifiers, returnType, false, paramTypes);
    }

    public MethodLogger(Random rand, String clazz, String name, int modifiers, FieldVarType<?> returnType, boolean inherited, FieldVarType... paramTypes) {
        super(rand);
        this.clazz = clazz;
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.variables = new HashMap<>();
        this.paramTypes = paramTypes;
        this.methodsExcludedForCalling = new HashSet<>();
        this.calledByThisMethod = new HashSet<>();
        this.inherited = inherited;
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
        return Arrays.stream(paramTypes).map(FieldVarType::getClazzType).toArray(CtClass[]::new);
    }

    public FieldVarType<?> getReturnType() {
        return returnType;
    }

    public boolean isVoid() {
        return returnType == VOID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodLogger that = (MethodLogger) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(paramTypes);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                TO_STRING_FORMAT,
                (inherited ? "@Inherited " : ""),
                Modifier.toString(modifiers),
                returnType.clazz.getCanonicalName(),
                name,
                Arrays.stream(paramTypes)
                        .map(ft -> ft.clazz)
                        .map(Class::getCanonicalName)
                        .collect(Collectors.joining(", ")));
    }

    public boolean isInherited() {
        return inherited;
    }
}
