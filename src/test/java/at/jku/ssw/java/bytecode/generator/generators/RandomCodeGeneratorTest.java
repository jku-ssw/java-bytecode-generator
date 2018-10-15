package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class RandomCodeGeneratorTest {
    private static final int REPETITIONS = 20;

    private static final boolean ALLOW_ARITHMETIC_EXCEPTIONS = true;

    private static final String DIRECTORY = "src/test/resources/generated_test_files";

    private static final Random RANDOM = new Random();

    private static List<String> getRandomArgs() {
        List<String> options = new ArrayList<>(Arrays.asList(
                "-l", "" + RANDOM.nextInt(10), "-f", "" + RANDOM.nextInt(30),
                "-lv", "" + RANDOM.nextInt(20), "-ga", "" + RANDOM.nextInt(20),
                "-la", "" + RANDOM.nextInt(20), "-m", "0" + RANDOM.nextInt(100),
                "-mc", "" + RANDOM.nextInt(40), "-ml", "" + (RANDOM.nextInt(10)),
                "-mp", "" + RANDOM.nextInt(10), "-mo", "0" + RANDOM.nextInt(100),
                "-p", "0", "-jlm", "" + RANDOM.nextInt(10),
                "-cf", "40", "-cl", "10",
                "-cd", "3", "-mli", "" + RANDOM.nextInt(6),
                "-while", "30", "-for", "30",
                "-dowhile", "20", "-if", "60",
                "-ibf", "10", "-os", "" + RANDOM.nextInt(30),
                "-as", "" + RANDOM.nextInt(100), "-ls", "" + RANDOM.nextInt(100),
                "-bs", "" + RANDOM.nextInt(100), "-als", "" + RANDOM.nextInt(100),
                "-abs", "" + RANDOM.nextInt(100), "-lbs", "" + RANDOM.nextInt(100),
                "-albs", "" + RANDOM.nextInt(100), "-mops", "" + RANDOM.nextInt(10),
                "-snippet", "" + RANDOM.nextInt(20),
                "-break", "" + RANDOM.nextInt(10), "-return", "" + RANDOM.nextInt(10),
                "-max_dim", "" + RANDOM.nextInt(10) + 1, "-max_dim_size", "" + RANDOM.nextInt(1_000)
        ));

        // optionally allow exceptions
        if (ALLOW_ARITHMETIC_EXCEPTIONS && RANDOM.nextBoolean()) {
            options.add("-of");
            options.add("-dz");
        }

        return options;
    }

    @RepeatedTest(value = REPETITIONS)
    void testGenerate(RepetitionInfo repetitionInfo) throws Exception {
        final String className = "HighBranchingFactor" + repetitionInfo.getCurrentRepetition();
        List<String> options = getRandomArgs();
        System.out.println("Generating class '" + className + "'");
        System.out.println("Options: ");

        final String valueOptionFormat = "\t%15s %-4d\n";
        final String noValueOptionFormat = "\t%15s\n";

        // print options
        IntStream.range(0, options.size() / 2).forEach(i -> {
            String opt = options.get(2 * i);

            Optional<String> optVal = 2 * i + 1 > options.size()
                    ? Optional.empty()
                    : Optional.of(options.get(2 * i + 1));

            if (optVal.isPresent()) {
                String val = optVal.get();
                if (val.startsWith("-")) {
                    System.out.format(noValueOptionFormat, opt);
                    System.out.format(noValueOptionFormat, Integer.parseInt(val));
                } else {
                    System.out.format(valueOptionFormat, opt, Integer.parseInt(val));
                }
            } else
                System.out.format(noValueOptionFormat, opt);
        });

        ControlValueParser parser = new ControlValueParser(options.toArray(new String[0]));
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(className, controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile(DIRECTORY);

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

        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File(DIRECTORY));
        BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = brIn.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = brErr.readLine()) != null) {
            if (!line.startsWith(" ")) continue;
            if (!checkIfExceptionAllowed(line, allowedExceptions)) {
                throw new IOException("Execution of " + fileName + " failed.\n" +
                        "Not allowed exception: " + line);
            }
        }
        if (!p.waitFor(1, TimeUnit.MINUTES) && allowedExceptions.length == 0) {
            throw new IOException("Execution of " + fileName + " failed ");
        } else {
            brIn.close();
            brErr.close();
            return true;
        }
    }

    private boolean checkIfExceptionAllowed(String line, String[] allowedExceptions) {
        for (String exception : allowedExceptions) {
            System.out.println(exception);
            System.out.println(line);
            if (line.contains(exception)) {
                return true;
            }
        }
        return false;
    }
}
