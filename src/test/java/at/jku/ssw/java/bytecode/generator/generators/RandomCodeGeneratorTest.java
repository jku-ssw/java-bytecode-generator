package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.CLIArgumentsProvider;
import at.jku.ssw.java.bytecode.generator.GeneratedClass;
import at.jku.ssw.java.bytecode.generator.GeneratorTest;
import at.jku.ssw.java.bytecode.generator.Result;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import static at.jku.ssw.java.bytecode.generator.BytecodeComparator.assertSameStructure;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class RandomCodeGeneratorTest implements GeneratorTest {

    private static final Logger logger = LogManager.getLogger();

    private static final int REPETITIONS = 50;
    private static final int MINOR_REPETITIONS = 10;

    private static final int MAX_LENGTH = 30;
    private static final boolean ALLOW_ARITHMETIC_EXCEPTIONS = true;

    private static final String DIR = "src/test/resources/generated";

    @BeforeEach
    void setUp() {
        TypeCache.CACHE.reset();
    }

    @ParameterizedTest
    @ArgumentsSource(MinorRepetitionProvider.class)
    @SuppressWarnings("unchecked")
    void testSeedGenerate(List<String> args, int index) throws Exception {
        int seed = new Random().nextInt();
        args.add("-seed");
        args.add(String.valueOf(seed));

        final GeneratedClass classA = generateClass("originals", "ASeededClass" + index, args);

        logger.info("Running class {}", classA);
        final Result exp = run(classA);

        // unload the class loader

        TypeCache.CACHE.reset();

        final GeneratedClass classB = generateClass("seeded", "ASeededClass" + index, args);

        logger.info("Running class {}", classB);
        final Result act = run(classB);

        compareResults(exp, act);

        byte[] classABytecode = Files.readAllBytes(Paths.get(DIR).resolve(classA.path).resolve(classA.name + ".class"));
        byte[] classBBytecode = Files.readAllBytes(Paths.get(DIR).resolve(classB.path).resolve(classB.name + ".class"));

        assertSameStructure(classABytecode, classBBytecode);
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
        logger.info("Generating class '{}'", className);

        GeneratedClass clazz = generateClass(className, args);

        logger.info("Executing class '{}'", clazz.name);

        logger.info("Running class {}", clazz);
        Result result = run(clazz);

        try {
            if (ALLOW_ARITHMETIC_EXCEPTIONS) {
                assertThat(
                        validateExceptions(result, ArithmeticException.class),
                        is(true)
                );
            }
        } catch (Throwable t) {
            fail(clazz, t);
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
