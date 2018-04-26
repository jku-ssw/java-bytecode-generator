package utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSupplier {
    static private int methodCharNum = 97;
    static private int varCharNum = 97;
    static private int varRepeat = 0;
    static private int methodRepeat = 0;
    private final static Random random = new Random();
    static private final String stringCandidates = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * @return a new unique variable-name
     */
    public static String getVarName() {
        if (varCharNum == 123) {
            varRepeat++;
            varCharNum = 97;
        }
        char c = (char) varCharNum;
        varCharNum++;
        String character = String.valueOf(c);
        String name = character;
        for (int i = 0; i < varRepeat; i++) {
            name = name + character;
        }
        return name;
    }

    /**
     * @return @return a new unique Method-name, if only names generated by RandomSupplier are used
     */
    public static String getMethodName() {
        if (methodCharNum == 123) {
            methodRepeat++;
            methodCharNum = 97;
        }
        char c = (char) methodCharNum;
        methodCharNum++;
        String character = String.valueOf(c);
        String name = "method" + character.toUpperCase();
        for (int i = 0; i < methodRepeat; i++) {
            name = name + character;
        }
        return name;
    }

    /**
     * @return a random FieldVarType
     */
    public static FieldVarType getFieldVarType() {
        int r = random.nextInt(FieldVarType.values().length - 1); //exclude void
        return FieldVarType.values()[r];
    }

    /**
     * @return an random array of ParameterTypes
     */
    public static FieldVarType[] getParameterTypes(int maxParameters) {
        int number = random.nextInt(maxParameters);
        FieldVarType[] types = new FieldVarType[number];
        for (int i = 0; i < number; i++) {
            types[i] = getFieldVarType();
        }
        return types;
    }

    /**
     * @return a random FieldType including type Void for ReturnType of a Method
     */
    public static FieldVarType getReturnType() {
        int r = random.nextInt(FieldVarType.values().length);
        return FieldVarType.values()[r];
    }

    /**
     * @param fieldVarType the fieldVarType of the value
     * @return a random value of given FieldVarType
     */
    public static Object getRandomValue(FieldVarType fieldVarType) {
        if (fieldVarType.getClazzType().getName().startsWith("java.lang")) {
            //for Objects 25% chance to be initialized with null
            if (random.nextInt(4) == 0) return null;
        }
        switch (fieldVarType) {
            case Byte:
                return (byte) random.nextInt();
            case Short:
                return (short) random.nextInt();
            case Int:
                return random.nextInt();
            case Long:
                return random.nextLong();
            case Float:
                return random.nextFloat();
            case Double:
                return random.nextDouble();
            case Boolean:
                return random.nextBoolean();
            case Char:
                return stringCandidates.charAt(random.nextInt(stringCandidates.length()));
            case String:
                return getString();
            default:
                return null;
        }
    }

    /**
     * @return a randomly generated String
     */
    public static String getString() {
        int length = random.nextInt(20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(stringCandidates.charAt(random.nextInt(stringCandidates.length())));
        }
        return sb.toString();
    }

    /**
     * @return random modifiers
     */
    public static int getModifiers() {
        int numberOfModifiers = random.nextInt(4);
        int[] modifiers = new int[numberOfModifiers];
        int r = random.nextInt(5);
        int[] exclude_access = {2, 3, 4};
        for (int i = 1; i < numberOfModifiers; i++) {
            switch (r) {
                case 0:
                    modifiers[i] = Modifier.STATIC;
                    r = nextIntWithExcludes(5, 0);
                    break;
                case 1:
                    modifiers[i] = Modifier.FINAL;
                    r = nextIntWithExcludes(5, 1);
                    break;
                case 2:
                    modifiers[i] = Modifier.PUBLIC;
                    r = nextIntWithExcludes(5, exclude_access);
                    break;
                case 3:
                    modifiers[i] = Modifier.PRIVATE;
                    r = nextIntWithExcludes(5, exclude_access);
                    break;
                case 4:
                    modifiers[i] = Modifier.PROTECTED;
                    r = nextIntWithExcludes(5, exclude_access);
                    break;
            }
        }
        return mergeModifiers(modifiers);
    }

    /**
     * helper function for excluding added modifiers
     *
     * @param range
     * @param excludes
     * @return
     */
    private static int nextIntWithExcludes(int range, int... excludes) {
        int r = +random.nextInt(range);
        for (int i = 0; i < excludes.length; i++) {
            if (excludes[i] > r) {
                return r;
            }
            r++;
        }
        return r;
    }

    /**
     * megeres the given array of modifiers into one Integer-variable
     *
     * @param modifiers the modifiers to merge
     * @return the merged modifiers
     */
    static int mergeModifiers(int[] modifiers) {
        if (modifiers.length == 0) return 0;

        int merged_modifiers = modifiers[0];
        for (int i = 1; i < modifiers.length; i++) {
            merged_modifiers |= modifiers[i];
        }
        return merged_modifiers;
    }

    /**
     * @param i the unique index of this parameter-variable-name
     * @return a unique parameter-variable-name, that is directly usable javassist's insertBefore/insertAfter-methods
     */
    public static String getParVarName(int i) {
        return "$" + i;
    }

    public static List<Object> getParamValues(FieldVarType[] paramTypes, MethodLogger method, ClazzLogger logger) {
        List<Object> values = new ArrayList<>();
        for (FieldVarType t : paramTypes) {
            if (random.nextBoolean()) { //add global variable
                if (!addFieldToParamValues(values, method, t, logger)) {
                    //add local variable if no global variable available
                    if (!addLocalVariableToParamValues(values, method, t)) {
                        //add random value if no variables available
                        values.add(RandomSupplier.getRandomValue(t));
                    }
                }
            } else { //add local variable
                if (!addLocalVariableToParamValues(values, method, t)) {
                    //add global variable if no local variable available
                    if (!addFieldToParamValues(values, method, t, logger)) {
                        //add random value if no variables available
                        values.add(RandomSupplier.getRandomValue(t));
                    }
                }
            }
        }
        return values;
    }

    private static boolean addFieldToParamValues(List<Object> values, MethodLogger method, FieldVarType t, ClazzLogger logger) {
        FieldVarLogger fvl = logger.getVariableOfType(t);
        if (fvl != null && (fvl.isStatic() || !method.isStatic())) {
            values.add(fvl);
            return true;
        } else return false;
    }

    private static boolean addLocalVariableToParamValues(List<Object> values, MethodLogger method, FieldVarType t) {
        FieldVarLogger fvl = method.getVariableOfType(t);
        if (fvl != null) {
            values.add(fvl);
            return true;
        } else return false;
    }

}
