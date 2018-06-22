import generators.RandomCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cli.ControlValueParser;
import cli.GenerationController;

import java.io.IOException;


public class TestRandomCodeGenerator extends TestGenerator {
    private String[] args = new String[]{
            "-l", "50", "-f", "0", "-lv", "100", "-ga", "40", "-la", "60", "-m", "100", "-mc", "100",
            "-ml", "0", "-mp", "7", "-mo", "0", "-p", "0", "jlm", "100", "-cf", "0", "-cl", "3", "-cd", "3", "-os", "0"};

    @Test
    void multipleCodeGeneration() {
        for (int i = 0; i < 10; i++) {
            //TODO random args
            //args = new String[]{};
            ControlValueParser parser = new ControlValueParser(args);
            GenerationController controller = parser.parse();
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("TestClazz" + i, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile("src/test/resources/generated_test_files");
            try {
                assertEquals(true, executeFile("TestClazz" + i));
            } catch (IOException | InterruptedException e) {
                throw new AssertionError(e);
            }
        }
    }
}
