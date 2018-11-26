package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.cli.GenerationController;
import at.jku.ssw.java.bytecode.generator.exceptions.CompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.resolvers.Resolver;
import at.jku.ssw.java.bytecode.generator.metamodel.resolvers.JavassistResolver;
import javassist.*;
import javassist.bytecode.ClassFilePrinter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;


public class ClazzFileContainer {

    private final CtClass clazz;
    private final ClazzLogger clazzLogger;
    private final RandomSupplier randomSupplier;
    private final String fileName;

    /**
     * The global resolver instance.
     */
    private final Resolver<String> resolver;

    public ClazzFileContainer(Random rand, GenerationController controller, String fileName) {
        this.clazz = ClassPool.getDefault().makeClass(fileName);

        this.resolver = new JavassistResolver();

        this.randomSupplier = new RandomSupplier(
                rand,
                controller.getMaxArrayDimensions(),
                controller.getMaxArrayDimensionSize(),
                controller.getPrimitiveTypesProbability(),
                controller.getObjectProbability(),
                controller.getArrayProbability(),
                controller.getVoidProbability(),
                controller.getArrayRestrictionProbability()
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
        this.clazzLogger = ClazzLogger.generate(
                rand,
                fileName,
                randomSupplier
        );
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

    public Resolver<String> resolver() {
        return resolver;
    }

    @Override
    public String toString() {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {

            ClassFilePrinter.print(clazz.getClassFile(), pw);

            return sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
