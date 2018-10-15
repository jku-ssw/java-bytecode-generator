package at.jku.ssw.java.bytecode.generator.loaders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Custom class loader that allows for generated classes to be used
 * directly.
 */
public class GeneratedClassLoader extends ClassLoader {

    private final String generationDirectory;

    public GeneratedClassLoader(String generationDirectory) {
        this.generationDirectory = generationDirectory;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] b = loadGeneratedClassFile(name);

        return defineClass(name, b, 0, b.length);
    }

    private byte[] loadGeneratedClassFile(String name) throws ClassNotFoundException {
        final String classFileName = name + ".class";
        // assume that the class is in the generated directory
        final Path classFile = Paths
                .get(generationDirectory)
                .resolve(classFileName);

        try {
            return Files.readAllBytes(classFile);
        } catch (IOException e) {
            throw new ClassNotFoundException(String.format(
                    "Generated class '%s' does not exist in output directory '%s'",
                    classFileName,
                    generationDirectory
            ));
        }
    }
}
