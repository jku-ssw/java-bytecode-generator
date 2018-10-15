package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.ClazzLogger;
import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Logger;

abstract class Generator {

    private static final Logger logger = Logger.getLogger(Generator.class.getName());

    final ClazzFileContainer clazzContainer;
    static final Random RANDOM = new Random();

    public Generator(ClazzFileContainer clazzContainer) {
        this.clazzContainer = clazzContainer;
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }

    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    public void writeFile() {
        try {
            this.getClazzFile().writeFile();
        } catch (NotFoundException | IOException | CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void writeFile(String pathname) {
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

    public CtMethod getCtMethod(MethodLogger method) {
        try {
            if (method.getName().equals("main")) {
                return this.getClazzFile().getDeclaredMethod(method.getName());
            } else {
                return this.getClazzFile().getDeclaredMethod(method.getName(), method.getCtParamTypes());
            }
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public RandomSupplier getRandomSupplier() {
        return getClazzContainer().getRandomSupplier();
    }

    void insertIntoMethodBody(MethodLogger method, String src) {
        if (src == null) {
            return;
        }
        try {
            CtMethod ctMethod = this.getCtMethod(method);
            ctMethod.insertAfter(src);
        } catch (CannotCompileException e) {
            logger.severe(src);
            throw new AssertionError(e);
        }
    }
}

