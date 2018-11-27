package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.base.VoidType.VOID;

/**
 * Represents a registered method that may be called from within the generated
 * class.
 *
 * @param <T> The Java class representing the return type of the method
 */
public class MethodLogger<T> extends Logger implements MethodBuilder<T> {
    //-------------------------------------------------------------------------
    // region Constants

    /**
     * The identifier that is used to describe the "main" method.
     */
    public static final String MAIN_NAME = "main";

    /**
     * The identifier that is used to describe the "run" method.
     */
    public static final String RUN_NAME = "run";

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
    private final MetaType<T> returnType;

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

    /**
     * Creates a new method logger.
     *
     * @param rand       The random instance
     *                   (enables passing over a given seed)
     * @param sender     The sender type
     * @param name       The method name
     * @param modifiers  The method modifiers
     * @param returnType The return type
     * @param paramTypes The parameter types
     */
    public MethodLogger(Random rand,
                        RefType<?> sender,
                        String name,
                        int modifiers,
                        MetaType<T> returnType,
                        MetaType<?>... paramTypes) {
        super(rand);
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.sender = sender;
        this.paramTypes = Arrays.asList(paramTypes);
        this.excludedCalls = new HashSet<>();
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
                sender,
                name,
                paramTypes.stream()
                        .map(MetaType::descriptor)
                        .collect(Collectors.joining(", ")));
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Property accessors

    /**
     * Prevents the given method from begin called from any path originating
     * from within this method's body.
     *
     * @param method The method to exclude
     */
    public void excludeCall(MethodLogger<?> method) {
        excludedCalls.add(method);
    }

    /**
     * Checks whether this method is excluded from being called from this
     * method's body. This is checked to prevent the generation of recursions.
     *
     * @param method The method to check
     * @return {@code true} if the methods match of if this method is excluded
     * in either this method or in any other method in the hierarchy;
     * {@code false} otherwise
     */
    public boolean isExcluded(MethodLogger<?> method) {
        return excludedCalls.contains(method) ||
                excludedCalls.stream().anyMatch(m -> m.isExcluded(method));
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
    public MetaType<T> returns() {
        return returnType;
    }

    // endregion
    //-------------------------------------------------------------------------
}
