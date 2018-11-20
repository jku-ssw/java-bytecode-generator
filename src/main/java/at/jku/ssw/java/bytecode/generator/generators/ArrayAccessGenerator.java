package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.BitSet;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.types.base.ArrayType.MIN_ARRAY_DIM_LENGTH;
import static at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind.ARRAY;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Assignments.assign;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Blocks.BlockEnd;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Blocks.If;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Conditions.notNull;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.array;

/**
 * Generator the enables access to arrays.
 * Those arrays already have to be generated.
 */
public class ArrayAccessGenerator extends MethodCaller {
    public ArrayAccessGenerator(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }

    /**
     * Generates a string that describes a valid array access.
     *
     * @param a    The array to access
     * @param dims The number of dimensions to access
     * @return a string representation of a the array access
     */
    private String srcAccessArray(FieldVarLogger<?> a, int dims) {
        assert dims > 0;

        BitSet[] restrictions = a.getType().getRestrictions();

        boolean unrestricted = !a.getType().isRestricted();

        return array(
                a.access(),
                IntStream.range(0, dims)
                        .map(d -> rand.ints(0, MIN_ARRAY_DIM_LENGTH)
                                // filter values that do not fit the restrictions (if any)
                                .filter(i -> unrestricted || restrictions[d] == null || restrictions[d].isEmpty() || restrictions[d].get(i))
                                .findFirst()
                                .orElseThrow(AssertionError::new))
                        .mapToObj(i -> cast(i).to(int.class))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Generates a source code string that describes a randomly generated
     * array read operation within the given method.
     *
     * @param method The method context
     * @return a string that contains the source code of the expression
     */
    public String srcGenerateArrayReadAccess(MethodLogger<?> method) {
        return new Randomizer(rand).shuffle(
                getClazzLogger()
                        .getInitializedVarsUsableInMethod(method)
                        // only array variables
                        .filter(v -> v.getType().kind() == ARRAY)
                        // only initialized arrays
                        .filter(FieldVarLogger::isInitialized)
                        .flatMap(a -> {
                            // fetch random number of dimensions
                            // (minimum 1, maximum the dimensions of a)
                            int nParams = rand.nextInt(a.getType().getDim()) + 1;

                            MetaType<?> returnType = ArrayType.resultingTypeOf(a, nParams);

                            return getClazzLogger()
                                    .getNonFinalVarsUsableInMethod(method)
                                    .filter(v -> v.getType().isAssignableFrom(returnType))
                                    .map(v -> (Supplier<String>) () ->
                                            If(notNull(a.toString())) +
                                                    Statement(
                                                            assign(srcAccessArray(a, nParams))
                                                                    .to(v.access())
                                                    ) +
                                                    BlockEnd
                                    );
                        })
        ).findFirst()
                .map(Supplier::get)
                .orElse("");
    }

    /**
     * Generates a source code string that describes a randomly generated
     * array write operation within the given method.
     *
     * @param method The method context
     * @return a string that contains the source code of the expression
     */
    public String srcGenerateArrayWriteAccess(MethodLogger<?> method) {
        return new Randomizer(rand).shuffle(
                getClazzLogger()
                        .getNonFinalVarsUsableInMethod(method)
                        .filter(v -> v.getType().kind() == ARRAY)
                        .filter(FieldVarLogger::isInitialized)
                        .flatMap(a -> {
                            // fetch random number of dimensions (at least,
                            // with a maximum of the dimensions of a)
                            int nParams = rand.nextInt(a.getType().getDim()) + 1;

                            MetaType<?> type = ArrayType.resultingTypeOf(a, nParams);

                            return getClazzLogger()
                                    .getInitializedVarsUsableInMethod(method)
                                    .filter(v -> type.isAssignableFrom(v.getType()))
                                    .map(v -> (Supplier<String>) () ->
                                            If(notNull(a.toString())) +
                                                    Statement(assign(v.access())
                                                            .to(srcAccessArray(a, nParams))
                                                    ) +
                                                    BlockEnd
                                    );
                        })
        ).findFirst()
                .map(Supplier::get)
                .orElse("");
    }
}
