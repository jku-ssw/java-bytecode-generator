package generator;

import javassist.*;
import utils.ClazzFileContainer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import utils.ClazzLogger;
import utils.Field;
import utils.FieldType;

/**
 * capable of generating the smallest executable class-file
 */
public class Generator {
    static final int MAX_LOCALS = 100;
    static final int MAX_GLOBALS = 100;
    static final int MAX_METHODS = 100;
    static final int MAX_METHOD_CALLS = 100;


    private static final List<FieldType> compWithShort = Arrays.asList(FieldType.Byte, FieldType.Short, FieldType.Char);
    private static final List<FieldType> compWithInt = Arrays.asList(FieldType.Byte, FieldType.Short, FieldType.Char, FieldType.Int);
    private static final List<FieldType> compWithLong = Arrays.asList(FieldType.Byte, FieldType.Short, FieldType.Char, FieldType.Int, FieldType.Long);
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
        switch (type) {
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

    static boolean isCompatibleTo(FieldType type1, FieldType type2) {
        switch (type1) {
            case Byte:
                if (type2 == FieldType.Byte) return true;
                break;
            case Short:
                if (compWithShort.contains(type2)) return true;
                else return false;
            case Int:
                if (compWithInt.contains(type2)) return true;
                else return false;
            case Long:
                if (compWithLong.contains(type2)) return true;
                else return false;
            case Float:
                if (type2 == FieldType.Float) return true;
                else return false;
            case Double:
                if (type2 == FieldType.Float || type2 == FieldType.Double) return true;
                else return false;
            case Boolean:
                if (type2 == FieldType.Boolean) return true;
                else return false;
            case Char:
                if (type2 == FieldType.Char || type2 == FieldType.Short) return true;
                else return false;
            case String:
                if (type2 == FieldType.String) return true;
                else return false;
        }
        return false;
    }

    private static <T> boolean contains2(final T[] array, final T v) {
        if (v == null) {
            for (final T e : array)
                if (e == null)
                    return true;
        } else {
            for (final T e : array)
                if (e == v || v.equals(e))
                    return true;
        }

        return false;
    }

    /**
     * @return the class-file of this generator
     */
    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    public CtMethod getMain() {
        return this.getMethod("main");
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

    static boolean validateModifiers(int[] modifiers) {
        int numberOfAccessModifiers = 0;
        for (int m : modifiers) {
            switch (m) {
                case Modifier.STATIC:
                    break;
                case Modifier.PUBLIC:
                    numberOfAccessModifiers++;
                    break;
                case Modifier.PRIVATE:
                    numberOfAccessModifiers++;
                    break;
                case Modifier.PROTECTED:
                    numberOfAccessModifiers++;
                    break;
                case Modifier.FINAL:
                    break;
                default:
                    return false;
            }
        }
        if (numberOfAccessModifiers > 1) return false;
        else return true;
    }

    public CtMethod getMethod(String methodName) {
        try {
            return this.getClazzFile().getDeclaredMethod(methodName);
        } catch (NotFoundException e) {
            System.err.println("Method " + methodName + " not found");
            e.printStackTrace();
            return null;
        }
    }
}

