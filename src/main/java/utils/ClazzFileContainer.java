package utils;

import javassist.*;
import logger.ClazzLogger;
import logger.MethodLogger;

/**
 * contains a ctClass-Object, that is exportable as a new Class-file
 * holds important information about the actual content of the Class-file
 */
public class ClazzFileContainer {

    private CtClass clazz;
    private ClazzLogger clazzLogger;
    private RandomSupplier randomSupplier;
    private String fileName;

    /**
     * generates a new minimal executable Class and a new ClazzLogger
     *
     * @param fielName the name of the generated Class
     */
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

        //TODO add correct parameter-logging for main, when arrays enabled
        MethodLogger main = new MethodLogger("main", Modifier.STATIC, FieldVarType.Void, new FieldVarType[0]);
        this.clazzLogger = new ClazzLogger();
        this.clazzLogger.setMain(main);
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

    public RandomSupplier getRandomSupplier() {
        return randomSupplier;
    }

    public String getFileName() {
        return fileName;
    }
}
