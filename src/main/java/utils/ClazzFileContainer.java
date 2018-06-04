package utils;

import javassist.*;
import logger.ClazzLogger;
import logger.MethodLogger;

/**
 * contains a CtClass-Object, that is exportable as a new Class-file
 * holds important information about the actual content of the Class-file
 */
public class ClazzFileContainer {

    private CtClass clazz;
    private ClazzLogger clazzLogger;
    private RandomSupplier randomSupplier;
    private String fileName;

    public ClazzFileContainer(String fielName) {
        this.clazz = getClazzPool().makeClass(fielName);
        this.randomSupplier = new RandomSupplier();
        this.fileName = fielName;
        this.clazzLogger = new ClazzLogger();
        createMinExecutableClazz();
    }

    public ClassPool getClazzPool() {
        return ClassPool.getDefault();
    }

    private void createMinExecutableClazz() {
        try {
            CtMethod m = CtNewMethod.make(
                    "public static void main(String[] args) {}",
                    this.clazz);
            clazz.addMethod(m);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }

        MethodLogger main = new MethodLogger("main", Modifier.STATIC, FieldVarType.Void);
        this.clazzLogger = new ClazzLogger();
        this.clazzLogger.setMain(main);
    }

    public CtClass getClazzFile() {
        return clazz;
    }

    public ClazzLogger getClazzLogger() {
        return clazzLogger;
    }

    public RandomSupplier getRandomSupplier() {
        return randomSupplier;
    }

    public String getFileName() {
        return fileName;
    }
}
