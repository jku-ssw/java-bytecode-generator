package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public interface GeneratorTest extends CLIArgumentsProvider {

    String outputDirectory();

    default String generateClass(String name, int iters, String... options) {
        // join passed options and defaults
        String[] allOpts = Stream.concat(
                Stream.of(
                        "-l", String.valueOf(iters),    // use `iters` iterations to generate the class
                        "-filename", name               // use file name
                ),
                Stream.of(options)
        ).toArray(String[]::new);

        ControlValueParser parser = new ControlValueParser(allOpts);
        GenerationController controller = parser.parse();

        final String className = controller.getFileName();

        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(className, controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile(outputDirectory());

        return className;
    }

    default void compareResults(Result expected, Result actual) {
        assertEquals(expected.out, actual.out);
        assertEquals(expected.err, actual.err);
    }

    default String generateClass(String name, String... options) {
        return generateClass(name, 1, options);
    }

    default String generateClass(String name, List<String> options) {
        return generateClass(name, options.toArray(new String[0]));
    }

    default void printArgs(List<String> args) {
        final String valueOptionFormat = "\t%15s %-4d\n";
        final String noValueOptionFormat = "\t%15s\n";

        // print options
        IntStream.range(0, args.size() / 2).forEach(i -> {
            String opt = args.get(2 * i);

            Optional<String> optVal = 2 * i + 1 > args.size()
                    ? Optional.empty()
                    : Optional.of(args.get(2 * i + 1));

            if (optVal.isPresent()) {
                String val = optVal.get();
                if (val.startsWith("-")) {
                    System.out.format(noValueOptionFormat, opt);
                    System.out.format(noValueOptionFormat, val);
                } else {
                    System.out.format(valueOptionFormat, opt, Integer.parseInt(val));
                }
            } else
                System.out.format(noValueOptionFormat, opt);
        });
    }

    default Result run(String className)
            throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("java " + className, null, new File(outputDirectory()));

        try (BufferedReader outStr = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader errStr = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

            if (!p.waitFor(1, TimeUnit.MINUTES)) {
                p.destroyForcibly();
                fail("Execution of " + className + " failed ");
            }

            String out = outStr.lines().collect(Collectors.joining());
            String err = errStr.lines().collect(Collectors.joining());

            return new Result(className, out, err);
        }
    }

    default boolean validateExceptions(Result result, Class<? extends Throwable>... allowedExceptions) {
        List<String> diff = Stream.of(result.err.split("\n"))
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .filter(l -> Arrays.stream(allowedExceptions)
                        .map(Class::getCanonicalName)
                        .noneMatch(l::contains))
                .collect(Collectors.toList());

        diff.forEach(l -> {
            System.out.println(result.err);
            fail("Execution of " + result.className + " failed - unexpected exception: " + l);
        });

        return diff.isEmpty();
    }
}
