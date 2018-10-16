package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.CLIArgumentsProvider;
import at.jku.ssw.java.bytecode.generator.GeneratorTest;
import at.jku.ssw.java.bytecode.generator.Result;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class RandomCodeGeneratorTest implements GeneratorTest {
    private static final int REPETITIONS = 50;
    private static final int MINOR_REPETITIONS = 10;

    private static final int MAX_LENGTH = 20;
    private static final boolean ALLOW_ARITHMETIC_EXCEPTIONS = true;

    private static final String DIR = "src/test/resources/generated";

    @ParameterizedTest
    @ArgumentsSource(MinorRepetitionProvider.class)
    @SuppressWarnings("unchecked")
    void testSeedGenerate(List<String> args) throws Exception {
        int seed = new Random().nextInt();
        System.out.println("Using seed " + seed);
        args.add("-seed");
        args.add(String.valueOf(seed));

        final String classA = generateClass("ASeededClass", args);

        final String classB = generateClass("AClassWithTheSameSeed", args);

        final Result exp = run(classA);
        final Result act = run(classB);

        compareResults(exp, act);
    }

    static class MinorRepetitionProvider implements CLIArgumentsProvider {

        @Override
        public int repetitions() {
            return MINOR_REPETITIONS;
        }

        @Override
        public boolean allowArithmeticExceptions() {
            return ALLOW_ARITHMETIC_EXCEPTIONS;
        }

        @Override
        public int maxLength() {
            return MAX_LENGTH;
        }
    }

    @ParameterizedTest
    @ArgumentsSource(RandomCodeGeneratorTest.class)
    @SuppressWarnings("unchecked")
    void testGenerate(List<String> args, int index) throws Exception {
        final String className = "HighBranchingFactor" + index;
        System.out.println("Generating class '" + className + "'");
        System.out.println("Options: ");

        printArgs(args);

        generateClass(className, args);

        System.out.println("Executing class '" + className + "'");

        Result result = run(className);

        if (ALLOW_ARITHMETIC_EXCEPTIONS) {
            assertThat(
                    validateExceptions(result, ArithmeticException.class),
                    is(true)
            );
        }
    }

    @Override
    public String outputDirectory() {
        return DIR;
    }

    @Override
    public int repetitions() {
        return REPETITIONS;
    }

    @Override
    public boolean allowArithmeticExceptions() {
        return ALLOW_ARITHMETIC_EXCEPTIONS;
    }

    @Override
    public int maxLength() {
        return MAX_LENGTH;
    }
}
