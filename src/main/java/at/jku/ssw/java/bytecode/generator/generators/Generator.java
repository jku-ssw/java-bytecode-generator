package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.exceptions.MethodCompilationFailedException;
import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.JavassistUtils;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import javassist.*;
import javassist.bytecode.BadBytecode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

abstract class Generator {

    private static final Logger logger = LogManager.getLogger();

    final ClazzFileContainer clazzContainer;
    protected final Random rand;

    public Generator(Random rand, ClazzFileContainer clazzContainer) {
        this.rand = rand;
        this.clazzContainer = clazzContainer;
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }

    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    public void writeFile() {
        writeFile(".");
    }

    public void writeFile(String pathname) {
        getClazzFile().getClassFile().getMethods()
                .forEach(m -> {
                    try {
                        m.rebuildStackMap(ClassPool.getDefault());
                    } catch (BadBytecode badBytecode) {
                        badBytecode.printStackTrace();
                    }
                });

        final Path path = Paths.get(pathname).resolve(getClazzFile().getName() + ".class");
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path.toFile()))) {
            this.getClazzFile()
                    .getClassFile()
                    .write(out);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public ClazzLogger getClazzLogger() {
        return this.clazzContainer.getClazzLogger();
    }

    public CtMethod getCtMethod(MethodLogger<?> method) {
        try {
            if (method.name().equals("main")) {
                return getClazzFile().getDeclaredMethod(method.name());
            } else {
                return getClazzFile().getDeclaredMethod(
                        method.name(),
                        method.argumentTypes().stream()
                                .map(JavassistUtils::toCtClass)
                                .toArray(CtClass[]::new));
            }
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public RandomSupplier getRandomSupplier() {
        return getClazzContainer().getRandomSupplier();
    }

    // TODO change parameter to expression and append to method body
    void insertIntoMethodBody(MethodLogger<?> method, String src) {
        if (src == null) {
            return;
        }
        try {
            CtMethod ctMethod = this.getCtMethod(method);
            ctMethod.insertAfter(src);
        } catch (CannotCompileException e) {
            logger.fatal(src);
            throw new MethodCompilationFailedException(method, e);
        }
    }
}

