package at.jku.ssw.java.bytecode.generator.loaders;

import at.jku.ssw.java.bytecode.generator.GeneratorTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratedClassLoaderTest implements GeneratorTest {

    private static final int REPETITIONS = 10;
    private static final String DIR = "src/test/resources/generated";
    private static final PathMatcher CLASS_FILE_MATCHER = FileSystems.getDefault().getPathMatcher("glob:*.class");

    private GeneratedClassLoader generatedClassLoader;

    @BeforeEach
    public void setUp() {
        generatedClassLoader = new GeneratedClassLoader(DIR);
    }

    @AfterEach
    public void tearDown() {
        generatedClassLoader = null;
    }

    @RepeatedTest(value = REPETITIONS)
    public void testLoadValidClass() throws Exception {
        String className = generateClass("AValidClass");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());
    }

    @RepeatedTest(value = REPETITIONS)
    public void testInstantiation() throws Exception {
        String className = generateClass("AnInstantiableClass");

        Class<?> clazz = generatedClassLoader.findClass(className);

        assertEquals(className, clazz.getCanonicalName());

        // class must be instantiable
        Object __ = clazz.newInstance();
    }

    @RepeatedTest(value = REPETITIONS)
    public void testLoadInValidClass() {
        final String className = "InvalidClass";

        assertThrows(
                ClassNotFoundException.class,
                () -> generatedClassLoader.findClass(className)
        );
    }

    @RepeatedTest(value = REPETITIONS)
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

    @RepeatedTest(value = REPETITIONS)
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

    @Override
    public String outputDirectory() {
        return DIR;
    }
}
