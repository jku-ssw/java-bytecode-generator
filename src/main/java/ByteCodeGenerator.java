import generator.RandomCodeGenerator;
import utils.ControlValueParser;
import utils.GenerationController;

public class ByteCodeGenerator {


    public static void main(String[] args) {
        ControlValueParser parser = new ControlValueParser(args);
        GenerationController controller = parser.parse();
        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator("MyGeneratedClazz", controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile();
    }
}
