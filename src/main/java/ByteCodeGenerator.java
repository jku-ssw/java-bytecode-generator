
import generator.RandomCodeGenerator;
import utils.ControlValueParser;
import utils.GenerationController;
import utils.RandomSupplier;

import java.lang.reflect.Modifier;
import java.util.Random;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();

        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("MyGeneratedClazz", controller);

        randomCodeGenerator.generate();

        randomCodeGenerator.writeFile();
    }
}
