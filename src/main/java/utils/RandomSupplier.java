package utils;

import java.lang.reflect.Modifier;
import java.util.Random;

public class RandomSupplier {
    private int methodCharNum = 97;
    private int varCharNum = 97;
    private int varRepeat = 0;
    private int methodRepeat = 0;
    static private final Random RANDOM = new Random();
    static private final String STRING_CANDIDATES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    static final int MAX_STRING_LENGTH = 20;

    public String getVarName() {
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

    public String getMethodName() {
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

    public static FieldVarType getFieldVarType() {
        int r = RANDOM.nextInt(FieldVarType.values().length - 1); //exclude void
        return FieldVarType.values()[r];
    }

    public static FieldVarType[] getParameterTypes(int maxParameters) {
        int n = RANDOM.nextInt(maxParameters);
        return getNParameterTypes(n);
    }

    public static FieldVarType[] getNParameterTypes(int n) {
        FieldVarType[] types = new FieldVarType[n];
        for (int i = 0; i < n; i++) {
            types[i] = getFieldVarType();
        }
        return types;
    }

    public static FieldVarType getReturnType() {
        int r = RANDOM.nextInt(FieldVarType.values().length);
        return FieldVarType.values()[r];
    }

    public static String getRandomValue(FieldVarType type) {
        if (type.getClazzType().getName().startsWith("java.lang")) {
            //for Objects 25% chance to be initialized with null
            if (RANDOM.nextInt(4) == 0) {
                return "null";
            }
        }
        return getRandomValueNotNull(type);
    }

    public static String getRandomValueNotNull(FieldVarType type) {
        switch (type) {
            case Byte:
                return "(byte)" + (byte) RANDOM.nextInt(Byte.MAX_VALUE + 1);
            case Short:
                return "(short)" + (short) RANDOM.nextInt(Short.MAX_VALUE + 1);
            case Int:
                return "" + RANDOM.nextInt();
            case Long:
                return RANDOM.nextLong() + "L";
            case Float:
                return RANDOM.nextFloat() + "f";
            case Double:
                return RANDOM.nextDouble() + "d";
            case Boolean:
                return "" + RANDOM.nextBoolean();
            case Char:
                return "\'" + STRING_CANDIDATES.charAt(RANDOM.nextInt(STRING_CANDIDATES.length())) + "\'";
            case String:
                return "\"" + getString() + "\"";
            default:
                return null;
        }
    }

    public static String getString() {
        int length = RANDOM.nextInt(MAX_STRING_LENGTH + 1);
        return getStringOfLength(length);
    }

    public static String getStringOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(STRING_CANDIDATES.charAt(RANDOM.nextInt(STRING_CANDIDATES.length())));
        }
        return sb.toString();
    }

    public static int getModifiers() {
        int numberOfModifiers = RANDOM.nextInt(4);
        int[] modifiers = new int[numberOfModifiers];
        int r = RANDOM.nextInt(5);
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

    private static int nextIntWithExcludes(int range, int... excludes) {
        int r = +RANDOM.nextInt(range);
        for (int i = 0; i < excludes.length; i++) {
            if (excludes[i] > r) {
                return r;
            }
            r++;
        }
        return r;
    }

    private static int mergeModifiers(int[] modifiers) {
        if (modifiers.length == 0) {
            return 0;
        }
        int merged_modifiers = modifiers[0];
        for (int i = 1; i < modifiers.length; i++) {
            merged_modifiers |= modifiers[i];
        }
        return merged_modifiers;
    }

    public String getParVarName(int i) {
        return "$" + i;
    }

}
