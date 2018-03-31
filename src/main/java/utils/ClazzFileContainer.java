package utils;

import javassist.*;

/**
 * contains a ctClass-Object, that is exportable as a new class-file
 * holds import information about the actual content of the class-file
 */
public class ClazzFileContainer {

    private CtClass clazz;

    private ClazzLogger clazzLogger;

    /**
     * generates a new minimal executable class and a new Clazzlogger
     * @param file_name the name of the generated class
     */
    public ClazzFileContainer(String file_name) {
        ClassPool pool = ClassPool.getDefault();
        this.clazz = pool.makeClass(file_name);
        this.clazzLogger = new ClazzLogger();
        createMinExecutableClazz();
    }

    /**
     * creates a minimal executable class-file
     */
    private void createMinExecutableClazz() {
        try {
            CtMethod m = CtNewMethod.make(
                    "public static void main(String[] args) {}",
                    this.clazz);
            clazz.addMethod(m);
        } catch (CannotCompileException e) {
            System.err.println("Cannot create minimal executable class");
            e.printStackTrace();
        }
        MethodLogger ml = new MethodLogger("main");
        clazzLogger.logMethod(ml);
    }

    /**
     *
     * @return returns the CtClass-Object of the clazz contained in this ClazzFileContainer
     */
    public CtClass getClazzFile() {
        return clazz;
    }


    /**
     * @return the ClazzLogger Object, which contains information about the generated clazz
     */
    public ClazzLogger getClazzLogger() {
        return clazzLogger;
    }


}
