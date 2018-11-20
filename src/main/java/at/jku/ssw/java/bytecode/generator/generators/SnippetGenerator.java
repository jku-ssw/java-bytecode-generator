package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.generators.snippets.AssignableFromCall;
import at.jku.ssw.java.bytecode.generator.generators.snippets.HashCodeSubtraction;
import at.jku.ssw.java.bytecode.generator.generators.snippets.Snippet;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generates predefined snippets of code to test vulnerable methods or
 * code sequences.
 */
public class SnippetGenerator extends Generator {

    /**
     * Contains all available snippet generators.
     */
    private static final List<Class<? extends Snippet>> GENERATORS = Arrays.asList(
            HashCodeSubtraction.class,
            AssignableFromCall.class
    );

    /**
     * Holds an instance of each generator to allow for fast checks and
     * invocations.
     */
    private final List<? extends Snippet> snippets;


    public SnippetGenerator(Random rand, RandomCodeGenerator codeGenerator) {
        super(rand, codeGenerator.getClazzFileContainer());

        snippets = GENERATORS.stream()
                .map(c -> {
                    try {
                        return c.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Inserts a randomly picked snippet into the method.
     *
     * @param method The method to which the snippet is added
     */
    public void generate(MethodLogger<?> method) {
        new Randomizer(rand)
                .shuffle(snippets.stream())
                .filter(s -> s.isPossible(method))
                .map(s -> s.generate(getRandomSupplier()))
                .findFirst()
                .ifPresent(src -> insertIntoMethodBody(method, src));
    }

}
