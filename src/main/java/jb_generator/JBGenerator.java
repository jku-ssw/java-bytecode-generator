package jb_generator;

import cli.ControlValueParser;
import cli.GenerationController;
import generators.RandomCodeGenerator;

public class JBGenerator {
    public static void main(String[] args) {
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(controller.getFileName(), controller);
        randomCodeGenerator.generate();
        if(controller.getLocation() != null) {
            randomCodeGenerator.writeFile(controller.getLocation());
        } else {
            randomCodeGenerator.writeFile();
        }
    }
}
