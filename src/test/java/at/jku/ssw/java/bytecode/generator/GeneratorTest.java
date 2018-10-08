package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;

import java.util.stream.Stream;

public interface GeneratorTest {

    String outputDirectory();

    default String generateClass(String name, int iters, String... options) {
        // join passed options and defaults
        String[] allOpts = Stream.concat(
                Stream.of(
                        "-l", String.valueOf(iters),    // use `iters` iterations to generate the class
                        "-filename", name               // use file name
                ),
                Stream.of(options)
        ).toArray(String[]::new);

        ControlValueParser parser = new ControlValueParser(allOpts);
        GenerationController controller = parser.parse();

        final String className = controller.getFileName();

        RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator(className, controller);
        randomCodeGenerator.generate();
        randomCodeGenerator.writeFile(outputDirectory());

        return className;
    }

    default String generateClass(String name, String... options) {
        return generateClass(name, 1, options);
    }
}
