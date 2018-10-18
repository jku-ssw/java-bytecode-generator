package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import javassist.*;

import java.util.Random;

public class ClazzFileContainer {

    private final CtClass clazz;
    private final ClazzLogger clazzLogger;
    private final RandomSupplier randomSupplier;
    private final String fileName;

    public ClazzFileContainer(Random rand, GenerationController controller, String fileName) {
        this.clazz = ClassPool.getDefault().makeClass(fileName);
        this.randomSupplier = new RandomSupplier(
                rand,
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
            throw new CompilationFailedException(e);
        }
        MethodLogger main = new MethodLogger(rand, "main", Modifier.STATIC, FieldVarType.VOID);
        this.clazzLogger = new ClazzLogger(rand, main, randomSupplier);
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
