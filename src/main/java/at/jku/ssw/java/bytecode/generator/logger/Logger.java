package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class Logger {

    protected final Random rand;

    protected Logger(Random rand) {
        this.rand = rand;
    }

    Map<String, FieldVarLogger<?>> variables;

    public <T> void logVariable(String name, String clazz, MetaType<T> type, int modifiers, boolean initialized, boolean isField) {
        FieldVarLogger<T> f = new FieldVarLogger<>(name, clazz, modifiers, type, initialized, isField);
        variables.put(name, f);
    }

    public boolean hasVariables() {
        return !variables.isEmpty();
    }

    public FieldVarLogger<?> getVariableWithPredicate(Predicate<FieldVarLogger<?>> predicate) {
        if (!hasVariables()) {
            return null;
        }
        List<FieldVarLogger<?>> predicateVars = getVariablesWithPredicate(predicate);
        if (predicateVars.isEmpty()) {
            return null;
        }
        return predicateVars.get(rand.nextInt(predicateVars.size()));
    }

    public List<FieldVarLogger<?>> getVariablesWithPredicate(Predicate<FieldVarLogger<?>> predicate) {
        return variables.values().stream().filter(
                predicate).collect(Collectors.toList());
    }

    public Stream<FieldVarLogger<?>> streamVariables() {
        return variables.values().stream();
    }
}
