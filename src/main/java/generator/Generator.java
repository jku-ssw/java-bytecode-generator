package generator;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import utils.ClazzFileContainer;
import utils.ClazzLogger;
import utils.MethodLogger;

import java.io.IOException;

/**
 * super-Class of all generators
 * capable of generating the smallest executable class-file
 */
class Generator {

    private ClazzFileContainer clazzContainer;

    /**
     * Takes an existing utils.ClazzFileContainer to extend
     *
     * @param clazzContainer the container for the class-file with additional information
     */
    public Generator(ClazzFileContainer clazzContainer) {
        this.clazzContainer = clazzContainer;
    }

    /**
     * creates a generator.Generator with a new utils.ClazzFileContainer
     *
     * @param filename name of the class-file to be generated
     */
    public Generator(String filename) {
        this.clazzContainer = new ClazzFileContainer(filename);
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }

    /**
     * @return the class-file processed by this generator
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

    /**
     * @return the ClazzLogger of the class, processed by this generator
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
}

