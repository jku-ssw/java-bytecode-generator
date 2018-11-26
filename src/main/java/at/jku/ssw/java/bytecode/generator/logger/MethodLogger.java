package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.operations.MethodCall;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;

import java.lang.reflect.Method;
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
public class MethodLogger<B> extends Logger implements MethodBuilder<B> {
    //-------------------------------------------------------------------------
    // region Constants

    public static final String MAIN_NAME = "main";
    public static final String RUN_NAME = "run";

    private static final String TO_STRING_FORMAT = "method %s %s %s(%s)";

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The method name (not the descriptor).
     */
    private final String name;

    /**
     * The modifiers.
     */
    private final int modifiers;

    /**
     * The parameter types that are required to call this method
     * (does not include sender).
     */
    private final List<MetaType<?>> paramTypes;

    /**
     * The type that is returned.
     */
    private final MetaType<B> returnType;

    /**
     * The type of the sender (i.e. the class in which this method
     * is defined).
     */
    private final RefType<?> sender;

    /**
     * The methods that are excluded from being called from within this
     * method's generated body.
     * This distinction is necessary to avoid infinite recursions by methods
     * calling each other mutually.
     */
    private final Set<MethodLogger<?>> excludedCalls;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    public MethodLogger(Random rand,
                        RefType<?> sender,
                        String name,
                        int modifiers,
                        MetaType<B> returnType,
                        MetaType<?>... paramTypes) {
        super(rand);
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.sender = sender;
        this.paramTypes = Arrays.asList(paramTypes);
        this.excludedCalls = new HashSet<>();
    }

    public MethodLogger(RefType<?> sender,
                        String name,
                        int modifiers,
                        MetaType<B> returnType,
                        MetaType<?>... paramTypes) {
        this(null, sender, name, modifiers, returnType, paramTypes);
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

    /**
     * Infers a new {@link MethodLogger} instance from the given reflective
     * method.
     *
     * @param method The reflective method
     * @param <T>    The returned Java class
     * @return a new method logger which encapsulates the reflective method
     */
    public static <T> MethodLogger<T> infer(Method method) {
        // TODO
        return null;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Object overrides

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodLogger<?> that = (MethodLogger<?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(paramTypes, that.paramTypes) &&
                Objects.equals(sender, that.sender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, paramTypes, sender);
    }

    /**
     * Returns a string representation of this object.
     * This implementation parses the actual method signature and returns it.
     */
    @Override
    public String toString() {
        return String.format(
                TO_STRING_FORMAT,
                Modifier.toString(modifiers),
                returnType.descriptor(),
                name,
                paramTypes.stream()
                        .map(MetaType::descriptor)
                        .collect(Collectors.joining(", ")));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Property accessors

    public void excludeCall(Set<MethodLogger<?>> method) {
        excludedCalls.addAll(method);
    }

    public Set<MethodLogger<?>> excludedCalls() {
        return new HashSet<>(excludedCalls);
    }

    // TODO replace all calls with #argumentTypes
    public MetaType[] getParamTypes() {
        return paramTypes.toArray(new MetaType[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefType<?> sender() {
        return sender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int modifiers() {
        return modifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetaType<?>> argumentTypes() {
        return paramTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetaType<?>> requires() {
        return isStatic()
                ? paramTypes
                : Stream.concat(
                Stream.<MetaType<?>>of(sender),
                paramTypes.stream()
        ).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Expression<B> build(List<Expression<?>> params) {
        if (isStatic())
            return new MethodCall.Static<>(
                    name,
                    returnType,
                    () -> sender,
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

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<B> returns() {
        return returnType;
    }

    // endregion
    //-------------------------------------------------------------------------
}
