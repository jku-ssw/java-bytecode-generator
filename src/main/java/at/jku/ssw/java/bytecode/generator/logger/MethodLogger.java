package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
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
     * The builders that are excluded from being called from within this
     * method's generated body.
     * This distinction is necessary to avoid infinite recursions by methods
     * calling each other mutually.
     */
    private final Set<MethodLogger<?>> exclusions;

    /**
     * The method body.
     */
    private final List<Expression<?>> body;

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
        this.exclusions = new HashSet<>();
        this.body = new ArrayList<>();
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
     * Invokes the given builder from this builder.
     * If the target builder is a {@link MethodLogger} this method
     * ensures that the targeted builder does not call this builder afterwards
     * to prevent infinite recursions.
     *
     * @param target The target builder (constructor, method etc.)
     */
    public final void invoke(Builder<?> target) {
        if (target instanceof MethodLogger) {
            MethodLogger<?> gen = (MethodLogger<?>) target;
            gen.exclude(this);
        }
    }

    /**
     * Excludes the given {@link MethodLogger} from being called from within
     * this builder (e.g. if the target already calls this builder).
     *
     * @param generator The generator that is to be excluded
     */
    public void exclude(MethodLogger<?> generator) {
        System.out.println(this + " excludes " + generator);
        exclusions.add(generator);
    }

    /**
     * Determines all local exclusions.
     *
     * @return a set of all local exclusions that directly call this builder
     */
    public Set<? extends MethodLogger<?>> exclusions() {
        return exclusions;
    }

    /**
     * Determines all builders that must not be called from within
     * this builder. This method recursively collects all builders
     * that are excluded from being called by this builder.
     *
     * @return a set of builders that must not be called by this builder
     */
    public final Set<? extends MethodLogger<?>> allExclusions() {
        Set<? extends MethodLogger<?>> e = buildExclusions(new HashSet<>());

        System.out.println("EXCLUSIONS FOR " + this);
        e.forEach(System.out::println);

        return e;
    }

    /**
     * Mutates the given set of generators and adds all this builder's
     * exclusions if it is not already excluded.
     *
     * @param generators The generator set that is modified
     * @return the modified set of generators (same as the parameter)
     */
    final Set<? extends MethodLogger<?>> buildExclusions(Set<MethodLogger<?>> generators) {

        // include this instance
        generators.add(this);

        // get all those instances that are new
        Set<? extends MethodLogger<?>> localExclusions =
                exclusions().stream()
                        .filter(generators::add)
                        .collect(Collectors.toSet());

        // call the excluded builders exclusions and add them
        localExclusions.forEach(e -> e.buildExclusions(generators));

        return generators;
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

    /**
     * Appends the given expression to the body.
     *
     * @param expression The expression to add
     * @return this builder (to enable chaining)
     */
    // TODO actually use in combination with "insertIntoMethodBody"
    public MethodLogger<T> append(Expression<?> expression) {
        body.add(expression);
        return this;
    }

    /**
     * Gets all expressions that form the body.
     *
     * @return a list of all expressions (in order) which form the body
     */
    public List<? extends Expression<?>> body() {
        return body;
    }

    // endregion
    //-------------------------------------------------------------------------
}
