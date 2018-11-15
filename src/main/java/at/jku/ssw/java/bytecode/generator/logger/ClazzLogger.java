package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.metamodel.base.Builder;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Expression;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ResolvedBuilder;
import at.jku.ssw.java.bytecode.generator.metamodel.impl.JavassistResolver;
import at.jku.ssw.java.bytecode.generator.types.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClazzLogger extends Logger {

    public final String name;
    private final List<MethodLogger> methods;
    private final MethodLogger main;
    private MethodLogger run;
    private final RandomSupplier randomSupplier;
    private final Randomizer randomizer;

    public ClazzLogger(Random rand, String name, MethodLogger main, RandomSupplier randomSupplier) {
        super(rand);
        this.name = name;
        this.methods = new ArrayList<>();
        // only use these if result should be non-deterministic
        // this.methods.add(new MethodLogger("hashCode", name, Modifier.PUBLIC, INT, true));
        // this.methods.add(new MethodLogger("toString", name, Modifier.PUBLIC, STRING, true));
        this.variables = new HashMap<>();
        this.main = main;
        this.randomSupplier = randomSupplier;
        this.randomizer = new Randomizer(rand);
    }

    public MethodLogger getMain() {
        return main;
    }

    public void setRun(MethodLogger run) {
        if (this.run == null) {
            this.run = run;
        }
    }

    public List<MethodLogger> getMethods() {
        return methods;
    }

    public MethodLogger getRun() {
        return this.run;
    }

    public void logMethod(MethodLogger ml) {
        methods.add(ml);
    }

    public List<MethodLogger> getOverloadedMethods(String name) {
        return methods.stream()
                .filter(m -> m.getName().equals(name))
                .collect(Collectors.toList());
    }

    public MethodLogger getRandomMethod() {
        if (hasMethods()) return methods.get(rand.nextInt(methods.size()));
        else return null;
    }

    public MethodLogger getRandomCallableMethod(MethodLogger callingMethod) {
        return randomizer
                .oneOf(getCallableMethods(callingMethod))
                .orElse(null);
    }

    private List<MethodLogger> getCallableMethods(MethodLogger callingMethod) {
        List<MethodLogger> callableMethods =
                callingMethod.isStatic()
                        ? getStaticMethods()
                        : new ArrayList<>(methods);

        callableMethods.remove(callingMethod);

        removeAllExcludedForCalling(callableMethods, callingMethod.getMethodsExcludedForCalling());

        return callableMethods;
    }

    public MethodLogger getRandomCallableMethodOfType(MethodLogger callingMethod, MetaType<?> metaType) {
        return randomizer
                .oneOf(getCallableMethods(callingMethod).stream()
                        .filter(m -> m.getReturnType() == metaType))
                .orElse(null);
    }

    private void removeAllExcludedForCalling(List<MethodLogger> callableMethods, Set<MethodLogger> excludedForCalling) {
        if (excludedForCalling.isEmpty())
            return;

        callableMethods.removeAll(excludedForCalling);

        excludedForCalling.forEach(m ->
                removeAllExcludedForCalling(
                        callableMethods,
                        m.getMethodsExcludedForCalling()
                )
        );
    }

    private List<MethodLogger> getStaticMethods() {
        return methods.stream()
                .filter(MethodLogger::isStatic)
                .collect(Collectors.toList());
    }

    public boolean hasMethods() {
        return !methods.isEmpty();
    }

    public ParamWrapper[] randomParameterValues(MetaType<?>[] paramTypes, MethodLogger method) {
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
     * @return an expression that evaluates to the given meta type
     */
    public <T> Expression<T> valueOf(MetaType<T> type, MethodLogger context) {
        // TODO enable invocation of methods, casts etc.
        List<Builder<T>> builders = type.builders();

        return randomizer.oneOf(builders.stream()
                .map(b -> new ResolvedBuilder<>(
                        b,
                        b.requires().stream()
                                .map(paramType ->
                                        Stream.<Stream<? extends Expression<?>>>of(
                                                getInitializedVarsUsableInMethod(context)
                                        ).<Expression<?>>flatMap(e -> e)
                                                .filter(v -> paramType.equals(v.type()))
                                                .findFirst()
                                                .orElseGet(() -> randomSupplier.constantOf(paramType)))
                                .collect(Collectors.toList()))))
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
    public Stream<ParamWrapper<?>> randomParameterValues(Stream<MetaType<?>> paramTypes, MethodLogger method) {
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


    public FieldVarLogger<?> getNonFinalFieldUsableInMethod(MethodLogger method) {
        if (method.isStatic())
            return getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        else
            return getVariableWithPredicate(v -> !v.isFinal());
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalCompatibleFieldUsableInMethod(MethodLogger method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<T>) getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() &&
                            type.isAssignableFrom(v.getType()));
        else
            return (FieldVarLogger<T>) getVariableWithPredicate(v ->
                    !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalInitializedCompatibleFieldUsableInMethod(MethodLogger method, MetaType<T> type) {
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
    public <T> FieldVarLogger<? extends T> getInitializedLocalVarOfType(MethodLogger method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                v.isInitialized() && v.getType() == type);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getInitializedCompatibleLocalVar(MethodLogger method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                v.isInitialized() && type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger<?> getNonFinalLocalVar(MethodLogger method) {
        return method.getVariableWithPredicate(v -> !v.isFinal());
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalCompatibleLocalVar(MethodLogger method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getInitializedFieldOfTypeUsableInMethod(MethodLogger method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isInitialized() && v.isStatic() && v.getType() == type);
        else
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isInitialized() && v.getType() == type);
    }

    public <T> FieldVarLogger<? extends T> getGlobalOrLocalVarInitializedOfTypeUsableInMethod(MethodLogger method, MetaType<T> type) {
        return randomizer.<FieldVarLogger<? extends T>>oneNotNullOf(
                () -> getInitializedLocalVarOfType(method, type),
                () -> getInitializedFieldOfTypeUsableInMethod(method, type)
        ).orElse(null);
    }

    public Stream<FieldVarLogger<?>> getNonFinalVarsUsableInMethod(MethodLogger method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(v -> !v.isFinal());
    }

    public Stream<FieldVarLogger<?>> getInitializedVarsUsableInMethod(MethodLogger method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(FieldVarLogger::isInitialized);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalFieldOfTypeUsableInMethod(MethodLogger method, MetaType<T> type) {
        if (method.isStatic())
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() && v.getType() == type);
        else
            return (FieldVarLogger<? extends T>) getVariableWithPredicate(v ->
                    !v.isFinal() && v.getType() == type);
    }

    @SuppressWarnings("unchecked")
    public <T> FieldVarLogger<? extends T> getNonFinalLocalVarOfType(MethodLogger method, MetaType<T> type) {
        return (FieldVarLogger<? extends T>) method.getVariableWithPredicate(v ->
                !v.isFinal() && v.getType() == type);
    }

}
