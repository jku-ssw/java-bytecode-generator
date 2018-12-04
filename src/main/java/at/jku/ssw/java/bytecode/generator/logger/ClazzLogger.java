package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.ResolvedBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.builders.MethodBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.expressions.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.resolvers.JavassistResolver;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.method;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.ternary;

/**
 * Describes the generated Java class.
 * In order to enable future extensions that generate multiple classes and
 * allow for dynamic invocation, the class is defined abstract
 * to generate dynamic overloads on demand.
 */
public class ClazzLogger
        extends Logger
        implements RefType<ClazzLogger> {

    //-------------------------------------------------------------------------
    // region Properties

    /**
     * The name of the generated class.
     */
    private final String name;

    /**
     * The fields that are available within this class.
     */
    private final List<FieldVarLogger<?>> fields;

    /**
     * The methods that are available within this class.
     */
    private final List<MethodLogger<?>> methods;

    /**
     * The main method.
     */
    private final MethodLogger<Void> main;

    /**
     * The run method.
     * This method is used to ensure that instance methods and fields
     * are always callable from at least one method.
     */
    private MethodLogger<Void> run;

    /**
     * Generator that provides random access to types and constants.
     */
    private final RandomSupplier supplier;

    /**
     * Randomizer instance for random selection of variants.
     */
    private final Randomizer randomizer;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Generates a new anonymous subclass of {@link ClazzLogger} that is
     * uniquely identifiable by its name and therefore
     * distinguishable from other generated classes.
     *
     * @param rand     The random instance
     * @param name     The (fully qualified) class name
     * @param supplier The supplier
     * @return a new {@link ClazzLogger} for the given class
     */
    public static ClazzLogger generate(final Random rand,
                                       final String name,
                                       final RandomSupplier supplier) {
        // register this type by name
        return CACHE.register(new ClazzLogger(rand, name, supplier));
    }

    /**
     * Initializes a new clazz logger with the given properties
     *
     * @param rand     The random instance
     * @param name     The (fully qualified) class name
     * @param supplier The supplier
     */
    private ClazzLogger(Random rand, String name, RandomSupplier supplier) {
        super(rand);

        assert rand != null;
        assert name != null;
        assert !name.isEmpty();
        assert supplier != null;

        this.name = name;
        this.supplier = supplier;

        this.fields = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.randomizer = new Randomizer(rand);

        // generate the main method
        this.main = MethodLogger.generateMainMethod(rand, this);
        this.run = MethodLogger.generateRunMethod(rand, this);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Object methods

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClazzLogger that = (ClazzLogger) o;
        return Objects.equals(name, that.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        return descriptor().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String descriptor() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return descriptor();
    }

    // endregion
    //-------------------------------------------------------------------------
    // region MetaType methods

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends ClazzLogger> clazz() {
        return getClass();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHashCode(FieldVarLogger<ClazzLogger> variable) {
        String name = variable.access();

        return ternary(
                notNull(name),
                method(method(method(name, "getClass"), "getSimpleName"), "hashCode"),
                "0L"
        );
    }

    /**
     * A generated class is only assignable from other generated classes
     * that extend it (therefore this lookup has to be dynamic).
     *
     * @param other The other type
     * @return {@code true} if the other type is a generated class inheriting
     * from this class or the same class; {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(MetaType<?> other) {
        return other == this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends ClazzLogger> getAssignableTypes() {
        return Collections.singletonList(this);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Registration methods

    /**
     * Registers the given method in this scope.
     *
     * @param method The method to register
     * @param <T>    The return type of the method
     * @return this context
     */
    public final <T> ClazzLogger register(MethodLogger<T> method) {
        methods.add(method);
        return this;
    }

    /**
     * Registers the given field in this scope.
     *
     * @param field The field to register
     * @param <T>   The field type
     * @return this context
     */
    public final <T> ClazzLogger register(FieldVarLogger<T> field) {
        fields.add(field);
        return this;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Random access

    /**
     * Generates an array of randomly selected parameter values that
     * correspond to the given type array. The values (e.g. fields, constants)
     * are selected from the given method's scope.
     *
     * @param paramTypes The required parameter types
     * @param method     The method context
     * @return an array of parameter mappings where the type of the value at
     * each position corresponds to the type at the position in the parameter
     * type array
     */
    public ParamWrapper[] randomParameterValues(MetaType<?>[] paramTypes, MethodLogger<?> method) {
        return randomParameterValues(Arrays.stream(paramTypes), method)
                .toArray(ParamWrapper[]::new);
    }

    /**
     * Generates an expression that corresponds to the given type.
     * The given context provides access to the current local variables.
     *
     * @param type    The required type that the expression should evaluate to
     * @param context The context in which this expression is placed
     * @param <T>     The actual Java class
     * @return an expression that evaluates to the given meta type or nothing
     * if no expression of the given class may be generated
     */
    public <T> Expression<T> valueOf(MetaType<T> type, MethodLogger<?> context) {
        // TODO enable invocation of methods, casts etc.
        List<Builder<T>> builders = type.builders();

        return randomizer.oneOf(
                builders.stream()
                        // remove builder that are already excluded (e.g. methods that
                        // already call the context method
                        .filter(context::isAllowed)
                        .flatMap(b -> {
                            List<? extends Expression<?>> params =
                                    b.requires().stream()
                                            .map(paramType ->
                                                    Stream.<Stream<? extends Expression<?>>>of(
                                                            getInitializedVarsUsableInMethod(context))
                                                            .<Expression<?>>flatMap(Function.identity())
                                                            .filter(v -> paramType.equals(v.type()))
                                                            .findFirst()
                                                            .orElse(
                                                                    // if no variable of the same type can be derived
                                                                    // try a constant
                                                                    supplier.constantOf(paramType)
                                                                            // if no constant can be generated,
                                                                            // signal that this builder is unusable
                                                                            .orElse(null)
                                                            ))
                                            .collect(Collectors.toList());

                            if (params.contains(null))
                                return Stream.empty();

                            return Stream.of(new ResolvedBuilder<>(b, params));
                        }))
                .map(b -> b.builder.build(b.params))
                .orElseThrow(() -> ErrorUtils.shouldNotReachHere("Could not resolve value for type " + type));
    }

    /**
     * Returns a stream that contains parameter values that map to the given
     * stream of expected parameter types.
     *
     * @param paramTypes The expected parameter types
     * @param method     The corresponding method
     * @return a stream of randomly picked parameter values - either variables
     * or constant values
     */
    public Stream<ParamWrapper<?>> randomParameterValues(Stream<? extends MetaType<?>> paramTypes, MethodLogger<?> method) {
        return paramTypes
                .map(t ->
                        randomizer.oneOf(
                                // try to find a suitable local variable, field or constant
                                getInitializedVarsUsableInMethod(method)
                                        .filter(v -> t.isAssignableFrom(v.getType()))
                                        .map((Function<FieldVarLogger<?>, ParamWrapper<?>>) ParamWrapper::new))
                                // or assign a constant value
                                .orElseGet(
                                        () -> new ParamWrapper<>(new JavassistResolver().resolve(valueOf(t, method)))
                                )
                );
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Random field access

    public FieldVarLogger<?> getNonFinalFieldUsableInMethod(MethodLogger<?> method) {
        if (method.isStatic())
            return getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        else
            return getVariableWithPredicate(v -> !v.isFinal());
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalCompatibleFieldUsableInMethod(MethodLogger<?> method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<T>) getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() &&
                            type.isAssignableFrom(v.getType()));
        else
            return (FieldVarLogger<T>) getVariableWithPredicate(v ->
                    !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalInitializedCompatibleFieldUsableInMethod(MethodLogger<?> method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isStatic() && v.isInitialized() && !v.isFinal() &&
                            type.isAssignableFrom(v.getType()));
        else
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    !v.isFinal() && v.isInitialized() &&
                            type.isAssignableFrom(v.getType()));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getInitializedLocalVarOfType(MethodLogger<?> method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                v.isInitialized() && v.getType() == type);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getInitializedCompatibleLocalVar(MethodLogger<?> method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                v.isInitialized() && type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger<?> getNonFinalLocalVar(MethodLogger<?> method) {
        return method.getVariableWithPredicate(v -> !v.isFinal());
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalCompatibleLocalVar(MethodLogger<?> method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getInitializedFieldOfTypeUsableInMethod(MethodLogger<?> method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isInitialized() && v.isStatic() && v.getType() == type);
        else
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isInitialized() && v.getType() == type);
    }

    public <T> FieldVarLogger<? extends T> getGlobalOrLocalVarInitializedOfTypeUsableInMethod(MethodLogger<?> method, MetaType<T> type) {
        return randomizer.<FieldVarLogger<? extends T>>oneNotNullOf(
                () -> getInitializedLocalVarOfType(method, type),
                () -> getInitializedFieldOfTypeUsableInMethod(method, type)
        ).orElse(null);
    }

    public Stream<FieldVarLogger<?>> getNonFinalVarsUsableInMethod(MethodLogger<?> method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(v -> !v.isFinal());
    }

    public Stream<? extends FieldVarLogger<?>> getInitializedVarsUsableInMethod(MethodLogger<?> method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(FieldVarLogger::isInitialized);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalFieldOfTypeUsableInMethod(MethodLogger<?> method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() && v.getType() == type);
        else
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    !v.isFinal() && v.getType() == type);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalLocalVarOfType(MethodLogger<?> method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                !v.isFinal() && v.getType() == type);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Random method access

    /**
     * Gets all methods that are available to this type
     * (e.g. all standard library methods, generated methods etc).
     *
     * @return a stream of all {@link MethodBuilder}s that are available
     * in this scope
     */
    public final Stream<? extends MethodBuilder<?>> allMethods() {
        return CACHE.refTypes()
                .map(RefType::methods)
                .flatMap(List::stream);
    }


    public List<MethodLogger<?>> getOverloadedMethods(String name) {
        return methods.stream()
                .filter(m -> m.name().equals(name))
                .collect(Collectors.toList());
    }

    /**
     * Selects an arbitrary method of all available generated methods
     * and library methods defined for standard types.
     *
     * @param filter An optional filter to preselect applicable methods
     * @return a randomly picked method that is available for this class
     */
    public Optional<? extends MethodBuilder<?>> randomMethod(Predicate<? super MethodBuilder<?>> filter) {
        return randomizer.oneOf(allMethods().filter(filter));
    }

    /**
     * @see #randomMethod(Predicate)
     */
    public Optional<? extends MethodBuilder<?>> randomMethod() {
        return randomizer.oneOf(allMethods());
    }

    /**
     * Selects one of the generated methods of this class and returns it.
     *
     * @param filter An optional filter to preselect applicable methods
     * @return a randomly picked generated method of this class or nothing
     * if no methods are generated yet
     */
    public final Optional<? extends MethodLogger<?>> randomGeneratedMethod(Predicate<? super MethodBuilder<?>> filter) {
        return randomizer.oneOf(methods.stream().filter(filter));
    }

    /**
     * @see #randomGeneratedMethod(Predicate)
     */
    public final Optional<MethodLogger<?>> randomGeneratedMethod() {
        return randomizer.oneOf(methods);
    }

    /**
     * Returns a randomly selected method that is callable from within the
     * given method.
     *
     * @return a method that is callable from within this class
     * or nothing if no methods can be found
     */
    public final Optional<? extends MethodBuilder<?>> randomCallableMethod(MethodLogger<?> caller) {
        return randomizer.oneOf(callableMethods(caller));
    }

    /**
     * Returns all methods that are callable withing the given calling method
     * (e.g. only static methods for static calls, preventing recursions).
     *
     * @param caller The calling method
     * @return a stream of {@link MethodBuilder}s which are callable
     * from within the given method
     */
    private Stream<? extends MethodBuilder<?>> callableMethods(MethodLogger<?> caller) {
        return allMethods()
                .filter(m -> !caller.isStatic() || m.isStatic())
                .filter(m -> !(m instanceof MethodLogger) ||
                        caller.isAllowed(m));
    }

    @SuppressWarnings("unchecked")
    public <T> MethodLogger<T> getRandomCallableMethodOfType(MethodLogger<?> callingMethod, MetaType<T> metaType) {
        return (MethodLogger<T>) randomizer
                .oneOf(callableMethods(callingMethod)
                        .filter(m -> m.returns() == metaType))
                .orElse(null);
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Property accessors

    /**
     * Returns the name of this class.
     *
     * @return a string describing the canonical name of this class
     */
    public final String name() {
        return name;
    }

    /**
     * The supplier that enable random access to types, constants
     * and generator properties.
     *
     * @return the random supplier
     */
    public final RandomSupplier supplier() {
        return supplier;
    }

    /**
     * Returns the main method.
     *
     * @return the {@link MethodLogger} that represents the main method
     */
    public final MethodLogger<Void> main() {
        return main;
    }

    /**
     * Returns the run method.
     *
     * @return the {@link MethodLogger} that represents the run method
     */
    public final MethodLogger<Void> run() {
        return run;
    }

    /**
     * Returns the fields of this class.
     *
     * @return a list of all fields that are registered for this class.
     */
    public final List<FieldVarLogger<?>> fields() {
        return fields;
    }

    /**
     * Returns the methods of this class.
     * TODO include / exclude main / run?
     *
     * @return a list of all methods that are registered for this class.
     * (excluding the {@link #main} and {@link #run} methods
     */
    public final List<MethodLogger<?>> methods() {
        return methods;
    }

    // endregion
    //-------------------------------------------------------------------------
}
