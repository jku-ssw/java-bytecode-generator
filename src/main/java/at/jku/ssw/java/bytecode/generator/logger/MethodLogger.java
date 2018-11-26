package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.MethodCall;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import javassist.CtClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.VoidType.VOID;

/**
 * Represents a registered method that may be called from within the generated
 * class.
 *
 * @param <B> The Java class representing the return type of the method
 */
public class MethodLogger<B> extends Logger implements Builder<B> {
    //-------------------------------------------------------------------------
    // region Constants

    public static final String MAIN_NAME = "main";
    public static final String RUN_NAME = "run";

    private static final String TO_STRING_FORMAT = "method %s%s %s %s(%s)";

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    private final boolean inherited;
    private final String name;
    private final int modifiers;
    private final MetaType[] paramTypes;
    private final MetaType<B> returnType;

    /**
     * The type of the container (i.e. the class in which this method
     * is defined).
     */
    private final MetaType<?> container;

    private final Set<MethodLogger<?>> methodsExcludedForCalling;
    private final Set<MethodLogger<?>> calledByThisMethod;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    public MethodLogger(Random rand, MetaType<?> container, String name, int modifiers, MetaType<B> returnType, MetaType<?>... paramTypes) {
        this(rand, container, name, modifiers, returnType, false, paramTypes);
    }

    public MethodLogger(Random rand, MetaType<?> container, String name, int modifiers, MetaType<B> returnType, boolean inherited, MetaType<?>... paramTypes) {
        super(rand);
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.container = container;
        this.paramTypes = paramTypes;
        this.methodsExcludedForCalling = new HashSet<>();
        this.calledByThisMethod = new HashSet<>();
        this.inherited = inherited;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Static utilities

    /**
     * Creates the main method.
     *
     * @param rand      The global random instance
     * @param container The containing class
     * @return a new {@link MethodLogger} that describes the main method
     */
    public static MethodLogger<Void> generateMainMethod(
            Random rand,
            ClazzLogger container) {

        return new MethodLogger<>(
                rand,
                container,
                MAIN_NAME,
                Modifier.STATIC,
                VOID,
                ArrayType.of(StringType.STRING, 1)
        );
    }

    /**
     * Creates the run method.
     *
     * @param rand      The global random instance
     * @param container The containing class
     * @return a new {@link MethodLogger} that describes the run method
     */
    public static MethodLogger<Void> generateRunMethod(
            Random rand,
            ClazzLogger container) {

        return new MethodLogger<>(
                rand,
                container,
                RUN_NAME,
                Modifier.PRIVATE,
                VOID
        );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Object overrides


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodLogger<?> that = (MethodLogger<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(container, that.container) &&
                Arrays.equals(paramTypes, that.paramTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, container);
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
                returnType.descriptor(),
                name,
                Arrays.stream(paramTypes)
                        .map(MetaType::descriptor)
                        .collect(Collectors.joining(", ")));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Property accessors

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

    public CtClass[] getCtParamTypes() {
        if (paramTypes == null) return new CtClass[0];
        return Arrays.stream(paramTypes)
                .map(JavassistUtils::toCtClass)
                .toArray(CtClass[]::new);
    }

    public boolean isInherited() {
        return inherited;
    }

    public boolean isVoid() {
        return returnType == VOID;
    }

    public MetaType<B> getReturnType() {
        return returnType;
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

    @Override
    public List<MetaType<?>> requires() {
        return isStatic()
                ? Arrays.asList(paramTypes)
                : Stream.concat(
                Stream.<MetaType<?>>of(container),
                Arrays.<MetaType<?>>stream(paramTypes)
        ).collect(Collectors.toList());
    }

    @Override
    public Expression<B> build(List<Expression<?>> params) {
        if (isStatic())
            return new MethodCall.Static<>(
                    name,
                    returnType,
                    () -> container,
                    params
            );

        // at least one parameter must be given (the instance)
        assert params.size() > 0;

        return new MethodCall<>(
                name,
                returnType,
                params.get(0),
                params.subList(1, params.size())
        );
    }

    @Override
    public MetaType<B> returns() {
        return returnType;
    }

    // endregion
    //-------------------------------------------------------------------------
}
