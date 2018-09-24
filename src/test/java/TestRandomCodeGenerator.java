import cli.ControlValueParser;
import cli.GenerationController;
import generators.RandomCodeGenerator;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestRandomCodeGenerator {
    private static String ARITHMETIC_EXCEPTIONS = "java.lang.ArithmeticException";

    private static boolean allowArithmeticExceptions;

    private static final String DIRECTORY = "src/test/resources/generated_test_files";

    private static final Random RANDOM = new Random();

    private static List<String> getRandomArgs() {
        List<String> options = new ArrayList<>(Arrays.asList(
                "-l", "10" + RANDOM.nextInt(20), "-f", "" + RANDOM.nextInt(100),
                "-lv", "" + RANDOM.nextInt(100), "-ga", "" + RANDOM.nextInt(100),
                "-la", "" + RANDOM.nextInt(100), "-m", "0",
                "-mc", "100", "-ml", "" + (RANDOM.nextInt(5)),
                "-mp", "" + RANDOM.nextInt(10), "-mo", "100",
                "-p", "" + RANDOM.nextInt(10), "-jlm", "0",
                "-cf", "100", "-cl", "" + RANDOM.nextInt(5),
                "-cd", "8", "-mli", "" + RANDOM.nextInt(6),
                "-while", "" + RANDOM.nextInt(100), "-for", "" + RANDOM.nextInt(100),
                "-dowhile", "" + RANDOM.nextInt(100), "-if", "" + RANDOM.nextInt(100),
                "-ibf", "" + RANDOM.nextInt(8), "-os", "100",
                "-as", "" + RANDOM.nextInt(100), "-ls", "" + RANDOM.nextInt(100),
                "-bs", "" + RANDOM.nextInt(100), "-als", "" + RANDOM.nextInt(100),
                "-abs", "" + RANDOM.nextInt(100), "-lbs", "" + RANDOM.nextInt(100),
                "-albs", "" + RANDOM.nextInt(100), "-mops", "" + RANDOM.nextInt(20)));

        allowArithmeticExceptions = RANDOM.nextBoolean();
        if (allowArithmeticExceptions) {
            options.add("-of");
            options.add("-dz");
        }

        return options;
    }

    @Test
    void multipleFileGeneration() {
        for (int i = 0; i < 100; i++) {
            List<String> options = getRandomArgs();
            ControlValueParser parser = new ControlValueParser(options.toArray(new String[0]));
            GenerationController controller = parser.parse();
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("DeeplyNestedControlFlow" + i, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile(DIRECTORY);
            try {
                if (allowArithmeticExceptions) {
                    assertEquals(true, executeFile("DeeplyNestedControlFlow" + i, ARITHMETIC_EXCEPTIONS));
                } else {
                    assertEquals(true, executeFile("DeeplyNestedControlFlow" + i));
                }
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }
    }

    boolean executeFile(String fileName, String... allowedExceptions) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File("src/test/resources/generated_test_files"));
        BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = brIn.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = brErr.readLine()) != null) {
            if (!line.startsWith(" ")) continue;
            if (checkIfExceptionAllowed(line, allowedExceptions)) {
                continue;
            } else {
                throw new IOException("Execution of " + fileName + " failed.\nNot allowed exception: " + line);
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
