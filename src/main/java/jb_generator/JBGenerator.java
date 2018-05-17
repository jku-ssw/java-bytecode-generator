package jb_generator;

import generators.RandomCodeGenerator;
import utils.cli.ControlValueParser;
import utils.cli.GenerationController;

public class JBGenerator {


    public static void main(String[] args) {
        for(int i = 0; i < args.length; i++) System.out.println(args[i]);
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("MyGeneratedClazz", controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile();
    }
}
