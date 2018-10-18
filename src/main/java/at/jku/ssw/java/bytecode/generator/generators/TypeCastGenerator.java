package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.Random;
import java.util.function.Predicate;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Assignments.assign;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;

public class TypeCastGenerator extends Generator {

    private final Randomizer randomizer;

    public TypeCastGenerator(Random rand, RandomCodeGenerator clazzContainer) {
        super(rand, clazzContainer.getClazzFileContainer());

        this.randomizer = new Randomizer(rand);
    }

    public void generatePrimitiveTypeCast(MethodLogger method) {
        ClazzLogger cl = getClazzLogger();

        Predicate<FieldVarLogger> isPrimitiveCastable = v ->
                v.getType().kind != FieldVarType.Kind.BOOLEAN &&
                        v.getType().kind != FieldVarType.Kind.INSTANCE &&
                        v.getType().kind != FieldVarType.Kind.VOID &&
                        v.getType().kind != FieldVarType.Kind.ARRAY;

        randomizer.shuffle(
                cl.getNonFinalVarsUsableInMethod(method)
                        .filter(isPrimitiveCastable)
                        .flatMap(dest ->
                                cl.getInitializedVarsUsableInMethod(method)
                                        .filter(isPrimitiveCastable)
                                        // only add type cast for variables that are actually of different types
                                        .filter(v -> !dest.getType().equals(v.getType()))
                                        .map(v ->
                                                Statement(assign(cast(v.getName()).to(dest.getType().clazz)).to(dest.getName()))))
        ).findAny().ifPresent(statement -> insertIntoMethodBody(method, statement));

    }
}
