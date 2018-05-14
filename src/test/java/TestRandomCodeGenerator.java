import generator.RandomCodeGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class TestRandomCodeGenerator {
    private String[] args = new String[]{
            "-l", "8", "-f", "10", "-lv", "5", "-ga", "40", "-la", "30", "-m", "20", "-mc", "30",
            "-ml", "2", "-mp", "6", "-p", "10", "-mo", "10", "-cf", "10", "-cl", "2", "-cd", "4"};

    @Test
    void testMultipleCodeGeneration() throws IOException, InterruptedException {
        //Process p;
        for (int i = 0; i < 100; i++) {
            //TODO random args
            ControlValueParser parser = new ControlValueParser(args);
            GenerationController controller = parser.parse();
            RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("Test" + i + "Clazz", controller);
            randomCodeGenerator.generate();
            randomCodeGenerator.writeFile("src/test/generated_files");

            Process p = Runtime.getRuntime().exec("java Test" + i + "Clazz", null, new File("src/test/generated_files"));
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                throw new IOException("Execution of Test" + i + "Clazz failed " + exitCode);
            } else assertEquals(true, new File("src/test/generated_files/Test" + i + "Clazz.class").delete());
        }
    }
}
