package utils;

import javassist.*;

/**
 * contains a ctClass-Object, that is exportable as a new class-file
 * holds import information about the actual content of the class-file
 */
public class ClazzFileContainer {

    private CtClass clazz;

    private ClazzLogger clazzLogger;

    public ClazzFileContainer(String file_name) {
        ClassPool pool = ClassPool.getDefault();
        this.clazz = pool.makeClass(file_name);
        this.clazzLogger = new ClazzLogger();
        createMinExecutableFile();
    }

    /**
     * creates a minimal executable class-file
     */
    private void createMinExecutableFile() {
        try {
            CtMethod m = CtNewMethod.make(
                    "public static void main(String[] args) {}",
                    this.clazz);
            clazz.addMethod(m);
        } catch (CannotCompileException e) {
            System.err.println("Cannot create minimal executable class-file");
            e.printStackTrace();
        }
        MethodLogger ml = new MethodLogger("main");
        clazzLogger.logMethod(ml);
    }

    public CtClass getClazzFile() {
        return clazz;
    }

    public CtMethod getMain() {
        try {
            return this.clazz.getDeclaredMethod("main");
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ClazzLogger getClazzLogger() {
        return clazzLogger;
    }


}
