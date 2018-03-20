package utils;
import javassist.*;

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

    private CtMethod main_;

    public ClazzFileContainer(String file_name) {
        ClassPool pool = ClassPool.getDefault();
        this.clazz = pool.makeClass(file_name);
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
            this.clazz.addMethod(m);
            main_ = this.getClazzFile().getDeclaredMethod("main");
        } catch (CannotCompileException | NotFoundException e) {
            System.err.println("Cannot create minimal executable class-file");
            e.printStackTrace();
        }
    }

    public CtClass getClazzFile() {
        return clazz;
    }

    public CtMethod getMain() {
        return main_;
    }
}
