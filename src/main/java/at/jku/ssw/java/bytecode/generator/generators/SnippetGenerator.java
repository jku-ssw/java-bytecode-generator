package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.generators.snippets.AssignableFromCall;
import at.jku.ssw.java.bytecode.generator.generators.snippets.HashCodeSubtraction;
import at.jku.ssw.java.bytecode.generator.generators.snippets.Snippet;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.Randomizer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SnippetGenerator extends Generator {

    private final List<Class<? extends Snippet>> snippetTypes = Arrays.asList(
            HashCodeSubtraction.class,
            AssignableFromCall.class
    );

    private final List<? extends Snippet> snippets;


    public SnippetGenerator(Random rand, RandomCodeGenerator codeGenerator) {
        super(rand, codeGenerator.getClazzFileContainer());

        snippets = snippetTypes.stream()
                .map(c -> {
                    try {
                        return c.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public void generate(MethodLogger method) {
        new Randomizer(rand)
                .shuffle(snippets.stream())
                .filter(s -> s.isPossible(method))
                .map(s -> s.generate(getRandomSupplier()))
                .findFirst()
                .ifPresent(src -> insertIntoMethodBody(method, src));
    }

}
