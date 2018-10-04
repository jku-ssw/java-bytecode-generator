package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
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


public class TestRandomCodeGenerator {
    private static String ARITHMETIC_EXCEPTIONS = "java.lang.ArithmeticException";

    private static boolean allowArithmeticExceptions;

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
                "-cf", "100", "-cl", "10",
                "-cd", "3", "-mli", "" + RANDOM.nextInt(6),
                "-while", "0", "-for", "0",
                "-dowhile", "0", "-if", "100",
                "-ibf", "10", "-os", "" + RANDOM.nextInt(30),
                "-as", "" + RANDOM.nextInt(100), "-ls", "" + RANDOM.nextInt(100),
                "-bs", "" + RANDOM.nextInt(100), "-als", "" + RANDOM.nextInt(100),
                "-abs", "" + RANDOM.nextInt(100), "-lbs", "" + RANDOM.nextInt(100),
                "-albs", "" + RANDOM.nextInt(100), "-mops", "" + RANDOM.nextInt(10),
                "-snippet", "" + RANDOM.nextInt(100)));

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
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("HighIfBranchingFactor" + i, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile(DIRECTORY);
//            try {
//                if (allowArithmeticExceptions) {
//                    assertEquals(true, executeFile("DeeplyNestedControlFlow" + i, ARITHMETIC_EXCEPTIONS));
//                } else {
//                    assertEquals(true, executeFile("DeeplyNestedControlFlow" + i));
//                }
//            } catch (IOException | InterruptedException e) {
//                throw new AssertionError(e);
//            }
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
