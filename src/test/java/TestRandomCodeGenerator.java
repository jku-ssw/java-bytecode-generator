import generators.RandomCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import utils.cli.ControlValueParser;
import utils.cli.GenerationController;

import java.io.IOException;


public class TestRandomCodeGenerator extends TestGenerator {
    private String[] args = new String[]{
            "-l", "10", "-f", "20", "-lv", "100", "-ga", "40", "-la", "60", "-m", "50", "-mc", "100",
            "-ml", "3", "-mp", "7", "-mo", "100", "-p", "5", "jlm", "100", "-cf", "30", "-cl", "3", "-cd", "3"};

    @Test
    void multipleCodeGeneration() throws IOException, InterruptedException {
        //Process p;
        for (int i = 0; i < 200; i++) {
            //TODO random args
            ControlValueParser parser = new ControlValueParser(args);
            GenerationController controller = parser.parse();
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("TestClazz" + i, controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile("src/test/generated_test_files");
            assertEquals(true, executeAndDeleteFile("TestClazz" + i));
        }
    }
}
