package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import javassist.CtClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.base.VoidType.VOID;

public class MethodLogger<T> extends Logger /*implements Builder<T>*/ {

    private static final String TO_STRING_FORMAT = "method %s%s %s %s(%s)";

    private final boolean inherited;
    private final String name;
    private final int modifiers;
    private final MetaType[] paramTypes;
    private final MetaType<T> returnType;

    /**
     * The type of the container (i.e. the class in which this method
     * is defined).
     */
    // TODO use
//    private final MetaType<T> container;

    private final Set<MethodLogger<?>> methodsExcludedForCalling;
    private final Set<MethodLogger<?>> calledByThisMethod;

    public final String clazz;

    public MethodLogger(Random rand, String clazz, String name, int modifiers, MetaType<T> returnType, /*MetaType<T> container, */MetaType<?>... paramTypes) {
        this(rand, clazz, name, modifiers, returnType, false, /*container, */paramTypes);
    }

    public MethodLogger(Random rand, String clazz, String name, int modifiers, MetaType<T> returnType, boolean inherited, /*MetaType<T> container, */MetaType<?>... paramTypes) {
        super(rand);
        this.clazz = clazz;
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
//        this.container = container;
        this.variables = new HashMap<>();
        this.paramTypes = paramTypes;
        this.methodsExcludedForCalling = new HashSet<>();
        this.calledByThisMethod = new HashSet<>();
        this.inherited = inherited;
    }

    public void addToExcludedForCalling(Set<MethodLogger<?>> excludedForCalling) {
        methodsExcludedForCalling.addAll(excludedForCalling);
    }

    public void addMethodToCalledByThisMethod(Set<MethodLogger<?>> calledByThisMethod) {
        this.calledByThisMethod.addAll(calledByThisMethod);
    }

    public Set<MethodLogger<?>> getMethodsExcludedForCalling() {
        return new HashSet<>(methodsExcludedForCalling);
    }

    public Set<MethodLogger<?>> getMethodsCalledByThisMethod() {
        return new HashSet<>(calledByThisMethod);
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public MetaType[] getParamTypes() {
        return paramTypes;
    }

    public CtClass[] getCtParamTypes() {
        if (paramTypes == null) return new CtClass[0];
        return Arrays.stream(paramTypes).map(MetaType::javassistClazz).toArray(CtClass[]::new);
    }

    public MetaType<T> getReturnType() {
        return returnType;
    }

    public boolean isVoid() {
        return returnType == VOID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodLogger<?> that = (MethodLogger<?>) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(paramTypes);
        return result;
    }

    /**
     * Returns a string representation of this object.
     * Parses the actual method signature and returns it.
     */
    @Override
    public String toString() {
        return String.format(
                TO_STRING_FORMAT,
                (inherited ? "@Inherited " : ""),
                Modifier.toString(modifiers),
                returnType.clazz().getCanonicalName(),
                name,
                Arrays.stream(paramTypes)
                        .map(MetaType::clazz)
                        .map(Class::getCanonicalName)
                        .collect(Collectors.joining(", ")));
    }

    public boolean isInherited() {
        return inherited;
    }

//    @Override
//    public List<? extends MetaType<?>> requires() {
//        return isStatic() ?
//                Arrays.asList(paramTypes) :
//                Stream.concat(Stream.of())
//                ;
//    }
//
//    @Override
//    public Expression<T> build(List<Expression<?>> params) {
//        return null;
//    }
//
//    @Override
//    public MetaType<T> returns() {
//        return returnType;
//    }
}
