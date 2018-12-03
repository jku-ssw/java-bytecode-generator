package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;

public class JBGenerator {

    public static void main(String[] args) {
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(controller.getFileName(), controller);
        randomCodeGenerator.generate();
        if (controller.getLocation() != null) {
            randomCodeGenerator.writeFile(controller.getLocation());
        } else {
            randomCodeGenerator.writeFile();
        }
    }
}
