package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public interface GeneratorTest extends CLIArgumentsProvider {

    Logger logger = LogManager.getLogger();

    String outputDirectory();

    default void fail(GeneratedClass clazz) {
        Assertions.fail(
                () -> "Test failed\n" +
                        "Command line arguments: " + clazz.args + "\n" +
                        "Seed: " + clazz.seed + "\n"
        );
    }

    default GeneratedClass generateClass(String name, String... options) {
        // join passed options and defaults
        String[] allOpts = Stream.concat(
                Stream.of(
                        "-filename", name               // use file name
                ),
                Stream.of(options)
        ).toArray(String[]::new);

        ControlValueParser parser = new ControlValueParser(allOpts);
        GenerationController controller = parser.parse();

        final String className = controller.getFileName();


        RandomCodeGenerator randomCodeGenerator;
        try {
            randomCodeGenerator = new RandomCodeGenerator(className, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile(outputDirectory());
        } catch (Throwable t) {
            logger.error("Generation failed");
            logger.error("Command line arguments: {}", String.join(" ", allOpts));
            logger.error("Seed: {}", controller.getSeedValue());
            throw t;
        }

        return new GeneratedClass(className, randomCodeGenerator.getSeed(), String.join(" ", allOpts));
    }

    default void compareResults(Result expected, Result actual) {
        assertEquals(expected.out, actual.out.replaceAll(actual.className, expected.className));
        assertEquals(expected.err, actual.err.replaceAll(actual.className, expected.className));
    }

    default GeneratedClass generateClass(String name, List<String> options) {
        return generateClass(name, options.toArray(new String[0]));
    }

    default Result run(GeneratedClass clazz)
            throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("java " + clazz.name, null, new File(outputDirectory()));

        try (BufferedReader outStr = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader errStr = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

            if (!p.waitFor(1, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                fail(clazz);
            }

            String out = outStr.lines().collect(Collectors.joining());
            String err = errStr.lines().collect(Collectors.joining());

            return new Result(clazz.name, out, err);
        }
    }

    default boolean validateExceptions(Result result, Class... allowedExceptions) {
        List<String> diff = Stream.of(result.err.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> Arrays.stream(allowedExceptions)
                        .map(Class::getCanonicalName)
                        .noneMatch(l::contains))
                .collect(Collectors.toList());

        diff.forEach(l -> {
            logger.error(result.err);
            logger.error("Execution of " + result.className + " failed - difference in line: " + l);
        });

        return diff.isEmpty();
    }
}
