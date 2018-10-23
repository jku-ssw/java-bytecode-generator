package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
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
        // this.methods.add(new MethodLogger("hashCode", name, Modifier.PUBLIC, FieldVarType.INT, true));
        // this.methods.add(new MethodLogger("toString", name, Modifier.PUBLIC, FieldVarType.STRING, true));
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

    public MethodLogger getRandomCallableMethodOfType(MethodLogger callingMethod, FieldVarType<?> fieldVarType) {
        return randomizer
                .oneOf(getCallableMethods(callingMethod).stream()
                        .filter(m -> m.getReturnType() == fieldVarType))
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

    public ParamWrapper[] randomParameterValues(FieldVarType<?>[] paramTypes, MethodLogger method) {
        return randomParameterValues(Arrays.stream(paramTypes), method)
                .toArray(ParamWrapper[]::new);
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
    public Stream<ParamWrapper<?>> randomParameterValues(Stream<FieldVarType<?>> paramTypes, MethodLogger method) {
        return paramTypes
                .map(t ->
                        randomizer.oneOf(
                                // try to find a suitable local variable, field or constant
                                getInitializedVarsUsableInMethod(method)
                                        .filter(v -> t.isAssignableFrom(v.getType()))
                                        .map((Function<FieldVarLogger, ParamWrapper<?>>) ParamWrapper::new))
                                // or assign a constant value
                                .orElseGet(
                                        () -> new ParamWrapper<>(randomSupplier.castedValue(t))
                                )
                );
    }


    public FieldVarLogger getNonFinalFieldUsableInMethod(MethodLogger method) {
        if (method.isStatic())
            return getVariableWithPredicate(v -> v.isStatic() && !v.isFinal());
        else
            return getVariableWithPredicate(v -> !v.isFinal());
    }

    public FieldVarLogger getNonFinalCompatibleFieldUsableInMethod(MethodLogger method, FieldVarType<?> type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() &&
                            type.isAssignableFrom(v.getType()));
        else
            return getVariableWithPredicate(v ->
                    !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger getNonFinalInitializedCompatibleFieldUsableInMethod(MethodLogger method, FieldVarType<?> type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isStatic() && v.isInitialized() && !v.isFinal() &&
                            type.isAssignableFrom(v.getType()));
        else
            return getVariableWithPredicate(v ->
                    !v.isFinal() && v.isInitialized() &&
                            type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger getInitializedLocalVarOfType(MethodLogger method, FieldVarType<?> type) {
        return method.getVariableWithPredicate(v ->
                v.isInitialized() && v.getType() == type);
    }

    public FieldVarLogger getInitializedCompatibleLocalVar(MethodLogger method, FieldVarType<?> type) {
        return method.getVariableWithPredicate(v ->
                v.isInitialized() && type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger getNonFinalLocalVar(MethodLogger method) {
        return method.getVariableWithPredicate(v -> !v.isFinal());
    }

    public FieldVarLogger getNonFinalCompatibleLocalVar(MethodLogger method, FieldVarType<?> type) {
        return method.getVariableWithPredicate(v ->
                !v.isFinal() && type.isAssignableFrom(v.getType()));
    }

    public FieldVarLogger getInitializedFieldOfTypeUsableInMethod(MethodLogger method, FieldVarType<?> type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isInitialized() && v.isStatic() && v.getType() == type);
        else
            return getVariableWithPredicate(v ->
                    v.isInitialized() && v.getType() == type);
    }

    public FieldVarLogger getGlobalOrLocalVarInitializedOfTypeUsableInMethod(MethodLogger method, FieldVarType<?> type) {
        return randomizer.oneNotNullOf(
                () -> getInitializedLocalVarOfType(method, type),
                () -> getInitializedFieldOfTypeUsableInMethod(method, type)
        ).orElse(null);
    }

    public Stream<FieldVarLogger> getNonFinalVarsUsableInMethod(MethodLogger method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(v -> !v.isFinal());
    }

    public Stream<FieldVarLogger> getInitializedVarsUsableInMethod(MethodLogger method) {
        return Stream.concat(
                method.streamVariables(),
                streamVariables()
                        .filter(f -> !method.isStatic() || f.isStatic())
        ).filter(FieldVarLogger::isInitialized);
    }

    public FieldVarLogger getNonFinalFieldOfTypeUsableInMethod(MethodLogger method, FieldVarType<?> type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() && v.getType() == type);
        else
            return getVariableWithPredicate(v ->
                    !v.isFinal() && v.getType() == type);
    }

    public FieldVarLogger getNonFinalLocalVarOfType(MethodLogger method, FieldVarType<?> type) {
        return method.getVariableWithPredicate(v ->
                !v.isFinal() && v.getType() == type);
    }

}
