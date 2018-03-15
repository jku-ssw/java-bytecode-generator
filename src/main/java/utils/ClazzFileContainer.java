package utils;

import javassist.ClassPool;
import javassist.CtClass;

/**
 * contains a ctClass-Object, that is exportable as a new class-file
 * holds import information about the actual content of the class-file
 */
public class ClazzFileContainer {
    private static final int MAX_LOCALS = 100;
    private static final int MAX_GLOBALS = 100;
    private static final int MAX_METHODS = 100;
    private static final int MAX_METHOD_CALLS = 100;


    private CtClass clazz;

    public ClazzFileContainer(String file_name) {
        ClassPool pool = ClassPool.getDefault();
        this.clazz = pool.makeClass(file_name);
    }

    public CtClass getClazzFile() {
        return clazz;
    }
}
