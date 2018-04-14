package utils;

import javassist.*;

/**
 * contains a ctClass-Object, that is exportable as a new Class-file
 * holds important information about the actual content of the Class-file
 */
public class ClazzFileContainer {

    private CtClass clazz;

    private ClazzLogger clazzLogger;

    /**
     * generates a new minimal executable Class and a new ClazzLogger
     *
     * @param file_name the name of the generated Class
     */
    public ClazzFileContainer(String file_name) {
        ClassPool pool = ClassPool.getDefault();
        this.clazz = pool.makeClass(file_name);
        this.clazzLogger = new ClazzLogger();
        createMinExecutableClazz();
    }

    /**
     * creates a minimal executable Class-file
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

        //TODO Add paramtypes, when Arrays enabled => then use args in Program too
        MethodLogger ml = new MethodLogger("main", Modifier.STATIC, FieldVarType.Void, null);
        clazzLogger.logMethod(ml);
    }

    /**
     * @return the CtClass-Object of the Clazz contained in this ClazzFileContainer
     */
    public CtClass getClazzFile() {
        return clazz;
    }


    /**
     * @return the ClazzLogger Object, which contains information about the generated Clazz
     */
    public ClazzLogger getClazzLogger() {
        return clazzLogger;
    }

}
