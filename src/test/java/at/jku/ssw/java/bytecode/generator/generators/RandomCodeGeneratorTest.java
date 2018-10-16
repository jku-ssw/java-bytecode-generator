package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.GeneratorTest;
import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;


public class RandomCodeGeneratorTest implements GeneratorTest {
    private static final int REPETITIONS = 20;
    private static final int MAX_LENGTH = 20;
    private static final boolean ALLOW_ARITHMETIC_EXCEPTIONS = true;

    private static final String DIR = "src/test/resources/generated";

    @ParameterizedTest
    @ArgumentsSource(RandomCodeGeneratorTest.class)
    void testGenerate(List<String> args, int index) throws Exception {
        final String className = "HighBranchingFactor" + index;
        System.out.println("Generating class '" + className + "'");
        System.out.println("Options: ");

        printArgs(args);

        ControlValueParser parser = new ControlValueParser(args.toArray(new String[0]));
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(className, controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile(DIR);

        System.out.println("Executing class '" + className + "'");
        if (ALLOW_ARITHMETIC_EXCEPTIONS) {
            assertThat(
                    executeFile(className, ArithmeticException.class.getCanonicalName()),
                    is(true)
            );
        } else {
            assertThat(
                    executeFile(className),
                    is(true)
            );
        }
    }

    private boolean executeFile(String fileName, String... allowedExceptions)
            throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File(DIR));

        try (BufferedReader outStr = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader errStr = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

            String out = outStr.lines().collect(Collectors.joining());
            System.out.println(out);

            String err = errStr.lines().collect(Collectors.joining());

            Stream.of(err.split("\n"))
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .filter(l -> checkIfExceptionAllowed(l, allowedExceptions))
                    .forEach(l -> {
                        System.out.println(err);
                        fail("Execution of " + fileName + " failed - unexpected exception: " + l);
                    });
            if (!p.waitFor(1, TimeUnit.MINUTES) && allowedExceptions.length == 0) {
                p.destroyForcibly();
                fail("Execution of " + fileName + " failed ");
            }
        }

        return true;
    }

    private boolean checkIfExceptionAllowed(String line, String... allowedExceptions) {
        for (String exception : allowedExceptions) {
            System.out.println(exception);
            System.out.println(line);
            if (!line.trim().isEmpty() && line.contains(exception)) {
                return true;
            }
        }
        return false;
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
