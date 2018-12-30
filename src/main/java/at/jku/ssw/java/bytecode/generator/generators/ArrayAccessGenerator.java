package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;
import at.jku.ssw.java.bytecode.generator.utils.StatementDSL;

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
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Statement;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.array;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.stream;

/**
 * Generator the enables access to arrays.
 * Those arrays already have to be generated.
 */
public class ArrayAccessGenerator extends MethodCaller {
    public ArrayAccessGenerator(Random rand, ClazzFileContainer clazzContainer) {
        super(rand, clazzContainer);
    }


    /**
     * Generates a randomly filled array that denotes visitable positions
     * of the array. Both the number of dimensions as well as the actual
     * positions are randomly generated.
     *
     * @param array The array that is accessed
     * @return an array that denotes the visited positions for each array.
     * The array has a length of at least one and at most that of
     * {@code array#length - 1}.
     */
    private int[] genAccessPositions(FieldVarLogger<?> array) {
        assert array != null;

        // fetch random number of dimensions
        // (minimum 1, maximum the dimensions of a)
        final int nParams = rand.nextInt(array.getType().getDim()) + 1;

        final BitSet[] restrictions = array.getType().getRestrictions();

        final boolean unrestricted = !array.getType().isRestricted();

        return IntStream.range(0, nParams)
                .map(d -> rand.ints(0, MIN_ARRAY_DIM_LENGTH)
                        // filter values that do not fit the restrictions (if any)
                        .filter(i -> unrestricted
                                || restrictions[d] == null
                                || restrictions[d].isEmpty()
                                || restrictions[d].get(i))
                        .findFirst()
                        .orElseThrow(AssertionError::new))
                .toArray();
    }

    /**
     * Generates a guard that ensures that the given array is not {@code null}
     * at any of the given position and then embeds the body in it.
     *
     * @param array     The array that is accessed
     * @param positions The array positions that are visited
     *                  (in each dimension)
     * @param body      The body expression that performs the array access
     * @return an expression that guards the array from being null and then
     * performs the potentially unsafe operation
     */
    private String guardAccess(FieldVarLogger<?> array, int[] positions, String body) {
        final String arrayName = array.access();

        /*
        This generates the concatenated condition string that ensures all
        accessed dimension positions (except the last one) being not null
        */
        final String condition = IntStream.range(0, positions.length)
                .mapToObj(pos -> stream(copyOfRange(positions, 0, pos)))
                .map(IntStream::toArray)
                .map(curPos -> array(arrayName, curPos))
                .map(StatementDSL.Conditions::notNull)
                .collect(Collectors.joining(" && "));

        return If(condition) +
                Statement(body) +
                BlockEnd;
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
                            int[] positions = genAccessPositions(a);

                            MetaType<?> returnType = ArrayType.resultingTypeOf(a, positions.length);

                            return getClazzLogger()
                                    .getNonFinalVarsUsableInMethod(method)
                                    .filter(v -> v.getType().isAssignableFrom(returnType))
                                    .map(v -> (Supplier<String>) () ->
                                            guardAccess(
                                                    a,
                                                    positions,
                                                    assign(
                                                            array(
                                                                    a.access(),
                                                                    positions)
                                                    ).to(v.access())
                                            )

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
                            int[] positions = genAccessPositions(a);

                            MetaType<?> type = ArrayType.resultingTypeOf(a, positions.length);

                            return getClazzLogger()
                                    .getInitializedVarsUsableInMethod(method)
                                    .filter(v -> type.isAssignableFrom(v.getType()))
                                    .map(v -> (Supplier<String>) () ->
                                            guardAccess(
                                                    a,
                                                    positions,
                                                    assign(v.access()).to(
                                                            array(
                                                                    a.access(),
                                                                    positions
                                                            )
                                                    )
                                            )
                                    );
                        })
        ).findFirst()
                .map(Supplier::get)
                .orElse("");
    }
}
