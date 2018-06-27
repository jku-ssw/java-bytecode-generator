package utils;

import javassist.*;
import logger.ClazzLogger;
import logger.MethodLogger;

public class ClazzFileContainer {

    private final CtClass clazz;
    private final ClazzLogger clazzLogger;
    private final RandomSupplier randomSupplier;
    private final String fileName;

    public ClazzFileContainer(String fielName) {
        this.clazz = getClazzPool().makeClass(fielName);
        this.randomSupplier = new RandomSupplier();
        this.fileName = fielName;
        try {
            CtMethod m = CtNewMethod.make(
                    "public static void main(String[] args) {}",
                    this.clazz);
            clazz.addMethod(m);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }

        MethodLogger main = new MethodLogger("main", Modifier.STATIC, FieldVarType.VOID);
        this.clazzLogger = new ClazzLogger(main);
    }

    public ClassPool getClazzPool() {
        return ClassPool.getDefault();
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
