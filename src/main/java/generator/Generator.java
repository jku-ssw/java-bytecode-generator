package generator;

import javassist.*;
import utils.ClazzFileContainer;

import java.io.IOException;

import utils.ClazzLogger;
import utils.FieldType;

/**
 * capable of generating the smallest executable class-file
 */
public class Generator {
    static final int MAX_LOCALS = 100;
    static final int MAX_GLOBALS = 100;
    static final int MAX_METHODS = 100;
    static final int MAX_METHOD_CALLS = 100;

    private ClazzFileContainer clazzContainer;

    /**
     * Takes an existing utils.ClazzFileContainer to extend
     *
     * @param cf the container for the class-file with additional information
     */
    public Generator(ClazzFileContainer cf) {
        this.clazzContainer = cf;
    }

    /**
     * creates a generator.Generator with a new utils.ClazzFileContainer
     *
     * @param filename name of the class-file to be generated
     */
    public Generator(String filename) {
        this.clazzContainer = new ClazzFileContainer(filename);
    }

    /**
     * creates a generator.Generator with a new utils.ClazzFileContainer with a default filename
     */
    public Generator() {
        this.clazzContainer = new ClazzFileContainer("GenClazz");
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }


    /**
     * checks compatibility of given value and fieldtype
     *
     * @param value the value to check compatibility for
     * @param type  the type to which value is checked for compatibility
     * @return {@code true} if value is assignable to a field of given type, otherwise {@code false}
     */
    static boolean isAssignable(Object value, FieldType type) {
        switch (type.getName()) {
            case Byte:
                return value instanceof Byte;
            case Short:
                return value instanceof Short;
            case Int:
                return value instanceof Integer;
            case Long:
                return value instanceof Long;
            case Float:
                return value instanceof Float;
            case Double:
                return value instanceof Double;
            case Boolean:
                return value instanceof Boolean;
            case Char:
                return value instanceof Character;
            case String:
                return value instanceof String;
            default:
                return false;
        }
    }

    /**
     * @return the class-file of this generator
     */
    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    public CtMethod getMain() {
        return this.clazzContainer.getMain();
    }

    /**
     * write the CtClass-Object as a .class file
     */
    public void writeFile() {
        try {
            this.getClazzFile().writeFile();
        } catch (NotFoundException | IOException | CannotCompileException e) {
            System.err.println("Cannot write class-file");
            e.printStackTrace();
        }
    }

    public ClazzLogger getClazzLogger() {
        return this.clazzContainer.getClazzLogger();
    }
}

