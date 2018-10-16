package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
import at.jku.ssw.java.bytecode.generator.utils.ParamWrapper;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.*;
import java.util.stream.Collectors;

public class ClazzLogger extends Logger {

    private final List<MethodLogger> methods;
    private final MethodLogger main;
    private MethodLogger run;
    private final RandomSupplier randomSupplier;
    private final Randomizer randomizer;

    public ClazzLogger(Random rand, MethodLogger main, RandomSupplier randomSupplier) {
        super(rand);
        this.methods = new ArrayList<>();
        // only use these if result should be non-deterministic
        // this.methods.add(new MethodLogger("hashCode", Modifier.PUBLIC, FieldVarType.INT, true));
        // this.methods.add(new MethodLogger("toString", Modifier.PUBLIC, FieldVarType.STRING, true));
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
        List<MethodLogger> callableMethods = getCallableMethods(callingMethod).stream()
                .filter(m -> m.getReturnType() == fieldVarType)
                .collect(Collectors.toList());

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

    private boolean addFieldToParamValues(List<ParamWrapper> values, MethodLogger method, FieldVarType<?> type) {
        return Optional.ofNullable(getInitializedFieldOfTypeUsableInMethod(method, type))
                .map(fvl -> values.add(new ParamWrapper<>(fvl)))
                .isPresent();
    }

    private boolean addLocalVariableToParamValues(List<ParamWrapper> values, MethodLogger method, FieldVarType<?> type) {
        return Optional.ofNullable(getInitializedLocalVarOfType(method, type))
                .map(fvl -> values.add(new ParamWrapper<>(fvl)))
                .isPresent();
    }

    public ParamWrapper[] getParamValues(FieldVarType[] paramTypes, MethodLogger method) {
        List<ParamWrapper> values = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            if (rand.nextBoolean()) { //add global variable
                if (!addFieldToParamValues(values, method, t)) {
                    //add local variable if no global variable available
                    if (!addLocalVariableToParamValues(values, method, t)) {
                        //add RANDOM value if no variables available
                        values.add(new ParamWrapper<>(randomSupplier.castedValue(t)));
                    }
                }
            } else { //add local variable
                if (!addLocalVariableToParamValues(values, method, t)) {
                    //add global variable if no local variable available
                    if (!addFieldToParamValues(values, method, t)) {
                        //add RANDOM value if no variables available
                        values.add(new ParamWrapper<>(randomSupplier.castedValue(t)));
                    }
                }
            }
        }

        return values.toArray(new ParamWrapper[0]);
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

    public FieldVarLogger getInitializedFieldOfTypeUsableInMethod(MethodLogger method, FieldVarType type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isInitialized() && v.isStatic() && v.getType() == type);
        else
            return getVariableWithPredicate(v ->
                    v.isInitialized() && v.getType() == type);
    }

    public FieldVarLogger getGlobalOrLocalVarInitializedOfTypeUsableInMethod(MethodLogger method, FieldVarType type) {
        return randomizer.oneNotNullOf(
                () -> getInitializedLocalVarOfType(method, type),
                () -> getInitializedFieldOfTypeUsableInMethod(method, type)
        ).orElse(null);
    }

    public FieldVarLogger getNonFinalFieldOfTypeUsableInMethod(MethodLogger method, FieldVarType type) {
        if (method.isStatic())
            return getVariableWithPredicate(v ->
                    v.isStatic() && !v.isFinal() && v.getType() == type);
        else
            return getVariableWithPredicate(v ->
                    !v.isFinal() && v.getType() == type);
    }

    public FieldVarLogger getNonFinalLocalVarOfType(MethodLogger method, FieldVarType type) {
        return method.getVariableWithPredicate(v ->
                !v.isFinal() && v.getType() == type);
    }

}
