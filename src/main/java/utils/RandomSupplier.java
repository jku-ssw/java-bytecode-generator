package utils;

import java.lang.reflect.Modifier;
import java.util.Random;

public class RandomSupplier {

    static private int char_num = 97;
    static private int repeat = 0;
    private final static Random random = new Random();
    static private final String stringCandidates = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String getVarName() {
        if (char_num == 123) {
            repeat++;
            char_num = 97;
        }
        char c = (char) char_num;
        char_num++;
        String varname = String.valueOf(c);
        String name = varname;
        for (int i = 0; i < repeat; i++) {
            name = name + varname;
        }
        return name;
    }

    public static FieldVarType getFieldType() {
        int r = random.nextInt(FieldVarType.values().length);
        return FieldVarType.values()[r];
    }

    public static Object getValue(FieldVarType ft) {
        if(random.nextInt(FieldVarType.values().length) == 0) return null;
        switch (ft) {
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

    public static String getString() {
        int length = random.nextInt(20);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(stringCandidates.charAt(random.nextInt(stringCandidates.length())));
        }
        return sb.toString();
    }


    /**
     * @return returns random modifiers with modifier static always included
     */
    public static int[] getModifiers() {
        int numberOfModifiers = 1 + random.nextInt(3);
        int[] modifiers = new int[numberOfModifiers];
        modifiers[0] = Modifier.STATIC;
        if (numberOfModifiers == 1) return modifiers;

        boolean hasAccessModifier = false;
        boolean isFinal = false;
        for (int i = 1; i < numberOfModifiers; i++) {
            boolean final_ = random.nextBoolean();
            if (final_) {
                if (!isFinal) {
                    modifiers[i] = Modifier.FINAL;
                    continue;
                }
            }
            int r = random.nextInt(3);
            switch (r) {
                case 0:
                    if (hasAccessModifier) {
                        modifiers[i] = Modifier.FINAL;
                        break;
                    }
                    modifiers[i] = Modifier.PUBLIC;
                    hasAccessModifier = true;
                    break;
                case 1:
                    if (hasAccessModifier) {
                        modifiers[i] = Modifier.FINAL;
                        break;
                    }
                    modifiers[i] = Modifier.PRIVATE;
                    hasAccessModifier = true;
                    break;
                case 2:
                    if (hasAccessModifier) {
                        modifiers[i] = Modifier.FINAL;
                        break;
                    }
                    modifiers[i] = Modifier.PROTECTED;
                    hasAccessModifier = true;
                    break;
            }

        }
        return modifiers;
    }
}
