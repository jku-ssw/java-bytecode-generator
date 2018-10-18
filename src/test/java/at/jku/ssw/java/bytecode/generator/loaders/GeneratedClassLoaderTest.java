package at.jku.ssw.java.bytecode.generator.loaders;

import at.jku.ssw.java.bytecode.generator.GeneratedClass;
import at.jku.ssw.java.bytecode.generator.GeneratorTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratedClassLoaderTest implements GeneratorTest {

    private static final Logger logger = LogManager.getLogger();

    private static final int REPETITIONS = 100;
    private static final int MAX_LENGTH = 50;
    private static final boolean ALLOW_ARITHMETIC_EXCEPTIONS = false;

    private static final String DIR = "src/test/resources/generated";

    private GeneratedClassLoader generatedClassLoader;

    @BeforeEach
    public void setUp() {
        generatedClassLoader = new GeneratedClassLoader(DIR);
    }

    @AfterEach
    public void tearDown() {
        generatedClassLoader = null;
    }

    @ParameterizedTest
    @ArgumentsSource(GeneratedClassLoaderTest.class)
    public void testLoadValidClass(List<String> args, int index) throws Exception {
        GeneratedClass genClass = generateClass("AValidClass" + index, args);

        Class<?> clazz = generatedClassLoader.findClass(genClass.name);

        assertEquals(genClass.name, clazz.getCanonicalName());
    }

    @ParameterizedTest
    @ArgumentsSource(GeneratedClassLoaderTest.class)
    public void testInstantiation(List<String> args, int index) throws Exception {
        GeneratedClass genClass = generateClass("AnInstantiableClass" + index, args);

        Class<?> clazz = generatedClassLoader.findClass(genClass.name);

        logger.info("Created class {}", genClass);

        assertEquals(genClass.name, clazz.getCanonicalName());

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

    @ParameterizedTest
    @ArgumentsSource(GeneratedClassLoaderTest.class)
    public void testInvokeMainMethod(List<String> args, int index) throws Exception {
        GeneratedClass genClass = generateClass("AClassWithMainMethod" + index, args);

        Class<?> clazz = generatedClassLoader.findClass(genClass.name);

        logger.info("Created class {}", genClass);

        assertEquals(genClass.name, clazz.getCanonicalName());

        Method main = clazz.getDeclaredMethod("main", String[].class);

        // check that it really is the main method
        assertTrue(Modifier.isStatic(main.getModifiers()));
        assertTrue(Modifier.isPublic(main.getModifiers()));
        assertEquals(Void.TYPE, main.getReturnType());

        assertNull(main.invoke(null, (Object) new String[0]));
    }

    @ParameterizedTest
    @ArgumentsSource(GeneratedClassLoaderTest.class)
    public void testInvokeRunMethod(List<String> args, int index) throws Exception {
        GeneratedClass genClass = generateClass("AClassWithRunMethod" + index, args);

        Class<?> clazz = generatedClassLoader.findClass(genClass.name);

        logger.info("Created class {}", genClass);

        assertEquals(genClass.name, clazz.getCanonicalName());

        Method run = clazz.getDeclaredMethod("run");

        // check that it has the expected modifiers
        assertFalse(Modifier.isStatic(run.getModifiers()));
        assertEquals(Void.TYPE, run.getReturnType());

        // class must be instantiable
        Object instance = clazz.newInstance();

        run.setAccessible(true);

        assertNull(run.invoke(instance));
    }

    @Override
    public String outputDirectory() {
        return DIR;
    }

    @Override
    public int repetitions() {
        return REPETITIONS;
    }

    @Override
    public boolean allowArithmeticExceptions() {
        return ALLOW_ARITHMETIC_EXCEPTIONS;
    }

    @Override
    public int maxLength() {
        return MAX_LENGTH;
    }
}
