package at.jku.ssw.java.bytecode.generator.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Contains utilities that are specific to Javassist.
 */
public class JavassistUtils {

    /**
     * Determines and returns the {@link javassist.CtClass} instance
     * corresponding to the given class name.
     *
     * @param clazz The Java class type instance that should be mapped
     * @return the class type corresponding to the class name
     */
    public static CtClass toCtClass(Class<?> clazz) {
        assert clazz != null;
        try {
            return ClassPool.getDefault().get(clazz.getCanonicalName());
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }
}
