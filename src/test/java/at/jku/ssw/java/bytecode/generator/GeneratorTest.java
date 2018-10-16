package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface GeneratorTest extends CLIArgumentsProvider {

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

    default void printArgs(List<String> args) {
        final String valueOptionFormat = "\t%15s %-4d\n";
        final String noValueOptionFormat = "\t%15s\n";

        // print options
        IntStream.range(0, args.size() / 2).forEach(i -> {
            String opt = args.get(2 * i);

            Optional<String> optVal = 2 * i + 1 > args.size()
                    ? Optional.empty()
                    : Optional.of(args.get(2 * i + 1));

            if (optVal.isPresent()) {
                String val = optVal.get();
                if (val.startsWith("-")) {
                    System.out.format(noValueOptionFormat, opt);
                    System.out.format(noValueOptionFormat, val);
                } else {
                    System.out.format(valueOptionFormat, opt, Integer.parseInt(val));
                }
            } else
                System.out.format(noValueOptionFormat, opt);
        });
    }
}
