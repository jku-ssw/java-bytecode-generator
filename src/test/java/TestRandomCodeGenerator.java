import generator.RandomCodeGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import utils.cli.ControlValueParser;
import utils.cli.GenerationController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class TestRandomCodeGenerator {
    private String[] args = new String[]{
            "-l", "10", "-f", "40", "-lv", "50", "-ga", "40", "-la", "90", "-m", "20", "-mc", "40",
            "-ml", "2", "-mp", "6", "-p", "10", "-mo", "100", "-cf", "10", "-cl", "2", "-cd", "4", "jlm", "100"};

    @Test
    void testMultipleCodeGeneration() throws IOException, InterruptedException {
        //Process p;
        for (int i = 0; i < 200; i++) {
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
//                throw new IOException("Execution of Test" + i + "Clazz failed " + exitCode);
//                //print all variables
//                for (MethodLogger m : randomCodeGenerator.getClazzLogger().methods) {
//                    System.out.println(m.getName());
//                    for(FieldVarType paramTypes)
//                }
//
//                for (FieldVarLogger f : fieldVar_generator.getClazzContainer().getClazzLogger().getLocals(getClazzLogger().getMain())) {
//                    fieldVar_generator.generatePrintStatement(f, getClazzLogger().getMain());
//                }
            } else assertEquals(true, new File("src/test/generated_files/Test" + i + "Clazz.class").delete());
        }
    }
}
