package jb_generator;

import generators.RandomCodeGenerator;
import cli.ControlValueParser;
import cli.GenerationController;

public class JBGenerator {


    public static void main(String[] args) {
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("MyGeneratedClazz", controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile();
    }
}
