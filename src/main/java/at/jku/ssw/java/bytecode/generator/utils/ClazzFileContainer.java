package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import javassist.*;

public class ClazzFileContainer {

    private final CtClass clazz;
    private final ClazzLogger clazzLogger;
    private final RandomSupplier randomSupplier;
    private final String fileName;

    public ClazzFileContainer(GenerationController controller, String fileName) {
        this.clazz = ClassPool.getDefault().makeClass(fileName);
        this.randomSupplier = new RandomSupplier(
                controller.getMaxArrayDimensions(),
                controller.getMaxArrayDimensionSize(),
                controller.getPrimitiveTypesProbability(),
                controller.getObjectProbability(),
                controller.getArrayProbability(),
                controller.getVoidProbability()
        );

        this.fileName = fileName;
        try {
            CtMethod m = CtNewMethod.make(
                    "public static void main(String[] args) {}",
                    this.clazz);
            clazz.addMethod(m);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
        MethodLogger main = new MethodLogger("main", Modifier.STATIC, FieldVarType.VOID);
        this.clazzLogger = new ClazzLogger(main, randomSupplier);
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
