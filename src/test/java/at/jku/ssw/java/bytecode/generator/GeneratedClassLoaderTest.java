package at.jku.ssw.java.bytecode.generator;

import at.jku.ssw.java.bytecode.generator.cli.ControlValueParser;
import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.generators.RandomCodeGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratedClassLoaderTest {

    private static final String DIR = "src/test/resources/generated";
    private static final PathMatcher CLASS_FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.class");

    private GeneratedClassLoader generatedClassLoader;

    @BeforeEach
    public void setUp() throws Exception {
        generatedClassLoader = new GeneratedClassLoader(DIR);

        try (Stream<Path> pathStream = Files.walk(Paths.get(DIR))) {
            // delete any remaining class file
            pathStream
                    .filter(p -> CLASS_FILE_MATCHER.matches(p.getFileName()))
                    .filter(Files::isRegularFile)
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    @AfterEach
    public void tearDown() {
        generatedClassLoader = null;
    }

    @Test
    public void testLoadValidClass() throws Exception {
        String className = generateClass("AValidClass");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());
    }

    @Test
    public void testInstantiation() throws Exception {
        String className = generateClass("AnInstantiableClass");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());

        // class must be instantiable
        Object __ = clazz.newInstance();
    }

    @Test
    public void testLoadInValidClass() {
        final String className = "InvalidClass";

        assertThrows(
                ClassNotFoundException.class,
                () -> generatedClassLoader.findClass(className)
        );
    }

    @Test
    public void testInvokeMainMethod() throws Exception {
        String className = generateClass("AClassWithMainMethod");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());

        Method main = clazz.getDeclaredMethod("main", String[].class);

        // check that it really is the main method
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertEquals(Void.TYPE, main.getReturnType());

        assertNull(main.invoke(null, (Object) new String[]{}));
    }

    @Test
    public void testInvokeRunMethod() throws Exception {
        String className = generateClass("AClassWithRunMethod");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());

        Method run = clazz.getDeclaredMethod("run");

        // check that it has the expected modifiers
        assertFalse(Modifier.isStatic(run.getModifiers()));
        assertEquals(Void.TYPE, run.getReturnType());

        // class must be instantiable
        Object instance = clazz.newInstance();

        run.setAccessible(true);

        assertNull(run.invoke(instance));
    }

    private String generateClass(String name, int iters, String... options) {
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
        randomCodeGenerator.writeFile(DIR);

        return className;
    }

    private String generateClass(String name, String... options) {
        return generateClass(name, 1, options);
    }
}
