package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClassUtils;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.FieldVarType;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.Kind.ARRAY;
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
    private String srcAccessArray(FieldVarLogger a, int dims) {
        assert dims > 0;

        return array(
                a.access(),
                IntStream.range(0, a.getType().dim)
                        .map(i -> rand.nextInt(FieldVarType.MIN_ARRAY_DIM_LENGTH))
                        .mapToObj(i -> cast(i).to(int.class))
                        .collect(Collectors.toList())
        );
    }

    public String scrGenerateArrayReadAccess(MethodLogger method) {
        return new Randomizer(rand).shuffle(
                getClazzLogger()
                        .getInitializedVarsUsableInMethod(method)
                        // only array variables
                        .filter(v -> v.getType().kind == ARRAY)
                        // only initialized arrays
                        .filter(FieldVarLogger::isInitialized)
                        .flatMap(a -> {
                            Class<?> aClass = a.getType().clazz;

                            int dim = a.getType().dim;

                            // fetch random number of dimensions
                            // (minimum 1, maximum the dimensions of a)
                            int nParams = rand.nextInt(dim) + 1;

                            // determine the return type
                            // (e.g. accessing int[][][] with 2 parameters
                            // yields a 1-dimensional array
                            int remainingDim = dim - nParams;

                            FieldVarType<?> innerType = a.getType().inner;
                            Class<?> componentType = ClassUtils.nthComponentType(nParams, aClass)
                                    .orElseThrow(() ->
                                            new AssertionError(String.format(
                                                    "Mismatching dimensions: %d for %s",
                                                    nParams,
                                                    aClass
                                            )));

                            FieldVarType<?> returnType;
                            if (remainingDim == 0)
                                returnType = innerType;
                            else
                                returnType = new FieldVarType<>(
                                        componentType,
                                        remainingDim,
                                        a.getType().inner
                                );

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
        ).findAny()
                .map(Supplier::get)
                .orElse("");
    }

    private String srcGenerateArrayWriteAccess(MethodLogger method) {

        int[] a = new int[1];
        a[(short) 0] = 99;
        return null;
    }
}
