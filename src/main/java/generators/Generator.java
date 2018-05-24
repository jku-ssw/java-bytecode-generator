package generators;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import logger.ClazzLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.RandomSupplier;

import java.io.IOException;
import java.util.Random;

/**
 * super-Class of all generators
 * capable of generating the smallest executable class-file
 */
abstract class Generator {

    ClazzFileContainer clazzContainer;
    Random random = new Random();

    /**
     * Takes an existing utils.ClazzFileContainer to extend
     *
     * @param clazzContainer the container for the class-file with additional information
     */
    public Generator(ClazzFileContainer clazzContainer) {
        this.clazzContainer = clazzContainer;
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }

    /**
     * @return the class-file processed by this generators
     */
    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    /**
     * writes the CtClass-Object as a .class file
     */
    public void writeFile() {
        try {
            this.getClazzFile().writeFile();
        } catch (NotFoundException | IOException | CannotCompileException e) {
            System.err.println("Cannot write class-file");
            e.printStackTrace();
        }
    }

    public void writeFile(String directoryName) {
        try {
            this.getClazzFile().writeFile(directoryName);
        } catch ( IOException | CannotCompileException e) {
            System.err.println("Cannot write class-file");
            e.printStackTrace();
        }
    }

    /**
     * @return the ClazzLogger of the class, processed by this generators
     */
    public ClazzLogger getClazzLogger() {
        return this.clazzContainer.getClazzLogger();
    }

    /**
     * @param method the logger of the method to return
     * @return the CtMethod-Object of the method given by its MethodLogger
     */
    public CtMethod getCtMethod(MethodLogger method) {
        try {
            if (method.getName().equals("main")) return this.getClazzFile().getDeclaredMethod(method.getName());
            else return this.getClazzFile().getDeclaredMethod(method.getName(), method.getCtParamsTypes());
        } catch (NotFoundException e) {
            System.err.println("Method " + method.getName() + " not found");
            e.printStackTrace();
            return null;
        }
    }

    public RandomSupplier getRandomSupplier() {
        return getClazzContainer().getRandomSupplier();
    }

    //=================================================Utility==========================================================
    boolean insertIntoMethodBody(MethodLogger method, String src) {
        if (src == null) return false;
        try {
            CtMethod ctMethod = this.getCtMethod(method);
            ctMethod.insertAfter(src);
            return true;
        } catch (CannotCompileException e) {
            e.printStackTrace();
            return false;
        }
    }
}

