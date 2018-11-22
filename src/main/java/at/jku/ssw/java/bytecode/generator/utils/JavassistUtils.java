package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Contains utilities that are specific to Javassist.
 */
public class JavassistUtils {

    /**
     * Determines and returns the {@link javassist.CtClass} instance
     * corresponding to the given class name.
     *
     * @param className The name of the class that should be mapped
     * @return the class type corresponding to the class name
     */
    public static CtClass toCtClass(String className) {
        assert className != null;
        try {
            return ClassPool.getDefault().get(className);
        } catch (NotFoundException e) {
            throw ErrorUtils.shouldNotReachHere("Could not generate CtClass for '" + className + "'");
        }
    }

    /**
     * @see #toCtClass(String)
     */
    public static CtClass toCtClass(Class<?> clazz) {
        assert clazz != null;
        return toCtClass(clazz.getCanonicalName());
    }

    /**
     * @see #toCtClass(String)
     */
    public static CtClass toCtClass(MetaType<?> type) {
        switch (type.kind()) {
            case BYTE:
                return CtClass.byteType;
            case SHORT:
                return CtClass.shortType;
            case RINT:
            case INT:
                return CtClass.intType;
            case LONG:
                return CtClass.longType;
            case FLOAT:
                return CtClass.floatType;
            case DOUBLE:
                return CtClass.doubleType;
            case BOOLEAN:
                return CtClass.booleanType;
            case CHAR:
                return CtClass.charType;
            case VOID:
                return CtClass.voidType;
            default:
                break;
        }

        return toCtClass(type.descriptor());
    }
}
