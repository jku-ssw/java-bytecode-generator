import cli.ControlValueParser;
import cli.GenerationController;
import generators.RandomCodeGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestRandomCodeGenerator extends TestGenerator {
    private static String ARITHMETIC_EXCEPTIONS = "java.lang.ArithmeticException";

    private static boolean allowArithmeticExceptions;

    private static final String DIRECTORY = "src/test/resources/generated_test_files";

    private static final Random RANDOM = new Random();

    private static List<String> getRandomArgs() {
        List<String> options = new ArrayList<>(Arrays.asList(
                "-l", "" + RANDOM.nextInt(20), "-f", "" + RANDOM.nextInt(100),
                "-lv", "" + RANDOM.nextInt(100), "-ga", "" + RANDOM.nextInt(100),
                "-la", "" + RANDOM.nextInt(100), "-m", "" + RANDOM.nextInt(100),
                "-mc", "" + RANDOM.nextInt(100), "-ml", "" + RANDOM.nextInt(10),
                "-mp", "" + RANDOM.nextInt(10), "-mo", "" + RANDOM.nextInt(100),
                "-p", "" + RANDOM.nextInt(20), "jlm", "" + RANDOM.nextInt(100),
                "-cf", "" + RANDOM.nextInt(20), "-cl", "" + RANDOM.nextInt(10),
                "-cd", "" + RANDOM.nextInt(5), "-mli", "" + RANDOM.nextInt(3),
                "-while", "" + RANDOM.nextInt(100), "-for", "" + RANDOM.nextInt(100),
                "-dowhile", "" + RANDOM.nextInt(100), "-if", "" + RANDOM.nextInt(100),
                "-ibf", "" + RANDOM.nextInt(10), "-os", "" + RANDOM.nextInt(100),
                "-as", "" + RANDOM.nextInt(100), "-ls", "" + RANDOM.nextInt(100),
                "-bs", "" + RANDOM.nextInt(100), "-als", "" + RANDOM.nextInt(100),
                "-abs", "" + RANDOM.nextInt(100), "-lbs", "" + RANDOM.nextInt(100),
                "-albs", "" + RANDOM.nextInt(100), "-mops", "" + RANDOM.nextInt(10)));
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
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("TestClazz" + i, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile(DIRECTORY);
            try {
                if (allowArithmeticExceptions) {
                    assertEquals(true, executeFile("TestClazz" + i, ARITHMETIC_EXCEPTIONS));
                } else {
                    assertEquals(true, executeFile("TestClazz" + i));
                }
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }
    }
}
