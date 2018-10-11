package at.jku.ssw.java.bytecode.generator.utils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.BYTE;
import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.SHORT;

public class RandomSupplier {
    private int methodCharNum = 97;
    private int varCharNum = 97;
    private int varRepeat = 0;
    private int methodRepeat = 0;
    static private final Random RANDOM = new Random();
    static private final String STRING_CANDIDATES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_STRING_LENGTH = 20;
    public static final List<Integer> MODIFIERS = Arrays.asList(
            Modifier.STATIC,
            Modifier.FINAL,
            Modifier.SYNCHRONIZED,
            Modifier.PUBLIC,
            Modifier.PRIVATE,
            Modifier.PROTECTED
    );

    public String getVarName() {
        if (varCharNum == 123) {
            varRepeat++;
            varCharNum = 97;
        }
        char c = (char) varCharNum;
        varCharNum++;
        String character = String.valueOf(c);
        return IntStream.rangeClosed(0, varRepeat)
                .mapToObj(__ -> character)
                .collect(Collectors.joining());
    }

    public String getMethodName() {
        if (methodCharNum == 123) {
            methodRepeat++;
            methodCharNum = 97;
        }
        char c = (char) methodCharNum;
        methodCharNum++;
        String character = String.valueOf(c);
        return "method" + character.toUpperCase() + IntStream.range(0, methodRepeat)
                .mapToObj(__ -> character)
                .collect(Collectors.joining());
    }

    public static FieldVarType getFieldVarType() {
        int r = RANDOM.nextInt(FieldVarType.values.length - 1); //exclude void
        return FieldVarType.values[r];
    }

    public static FieldVarType[] getParameterTypes(int maxParameters) {
        int n = maxParameters == 0 ? 0 : RANDOM.nextInt(maxParameters);
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
        int r = RANDOM.nextInt(FieldVarType.values.length);
        return FieldVarType.values[r];
    }

    public static String getRandomCastedValue(FieldVarType type) {
        switch (type.kind) {
            case INSTANCE:
                // 25% chance for objects to be initialized with null
                if (RANDOM.nextInt(4) == 0) {
                    return "null";
                }
        }

        return getRandomCastedValueNotNull(type);
    }

    public static String getRandomCastedValueNotNull(FieldVarType type) {
        switch (type.kind) {
            case BYTE:
                return "(byte)" + (byte) RANDOM.nextInt();
            case SHORT:
                return "(short)" + (short) RANDOM.nextInt();
            case INT:
                return "" + RANDOM.nextInt();
            case LONG:
                return RANDOM.nextLong() + "L";
            case FLOAT:
                return RANDOM.nextFloat() + "f";
            case DOUBLE:
                return RANDOM.nextDouble() + "d";
            case BOOLEAN:
                return "" + RANDOM.nextBoolean();
            case CHAR:
                return "\'" + STRING_CANDIDATES.charAt(RANDOM.nextInt(STRING_CANDIDATES.length())) + "\'";
            case INSTANCE:
                if (type.clazz.equals(String.class)) {
                    return "\"" + getString() + "\"";
                } else if (type.clazz.equals(Date.class)) {
                    return "new java.util.Date(" + RANDOM.nextLong() + "L)";
                }
            default:
                throw new java.lang.AssertionError();
        }
    }

    public static String getRandomNumericValue(FieldVarType type, boolean notZero) {
        switch (type.kind) {
            case BYTE:
            case SHORT:
            case INT:
                int i = RANDOM.nextInt();
                if (type == BYTE) {
                    i = (byte) i;
                } else if (type == SHORT) {
                    i = (short) i;
                }
                if (notZero) {
                    return "" + (i != 0 ? i : ++i);
                } else {
                    return "" + i;
                }
            case LONG:
                long l = RANDOM.nextLong();
                if (notZero) {
                    return "" + (l != 0L ? l : ++l) + "L";
                } else {
                    return "" + l + "L";
                }
            case FLOAT:
                float f = RANDOM.nextFloat();
                if (notZero) {
                    return "" + (f != 0f ? f : ++f) + "f";
                } else {
                    return "" + f + "f";
                }
            case DOUBLE:
                double d = RANDOM.nextDouble();
                if (notZero) {
                    return "" + (d != 0d ? d : ++d) + "d";
                } else {
                    return "" + d + "d";
                }
            case CHAR:
                char c = STRING_CANDIDATES.charAt(RANDOM.nextInt(STRING_CANDIDATES.length()));
                if (notZero) {
                    return "\'" + (c != 0 ? c : ++c) + "\'";
                } else {
                    return "\'" + c + "\'";
                }
            default:
                throw new java.lang.AssertionError();
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

    /**
     * Returns a random modifier.
     *
     * @param exclusions Modifiers that should be excluded
     * @return a random modifier or none, if no modifier is available
     * (or all are excluded)
     */
    private static Optional<Integer> getRandomModifier(Set<Integer> exclusions) {
        List<Integer> possibleModifiers = MODIFIERS.stream()
                .filter(m -> !exclusions.contains(m))
                .collect(Collectors.toList());

        if (possibleModifiers.isEmpty())
            return Optional.empty();

        final int maxRange = possibleModifiers.size() - 1;

        if (maxRange == 0)
            return possibleModifiers.stream().findFirst();

        return possibleModifiers.stream()
                .skip(RANDOM.nextInt(maxRange))
                .findFirst();
    }

    /**
     * Generates a random modifier value.
     *
     * @param exclusions Excluded modifiers
     * @return a random integer describing modifiers
     */
    static int getModifiers(int... exclusions) {
        int numberOfModifiers = RANDOM.nextInt(4);
        int[] modifiers = new int[numberOfModifiers];

        Set<Integer> excluding = IntStream.of(exclusions)
                .boxed()
                .collect(Collectors.toSet());

        for (int i = 1; i < numberOfModifiers; i++) {
            int r = getRandomModifier(excluding).orElse(0);

            modifiers[i] = r;

            switch (r) {
                case Modifier.PUBLIC:
                case Modifier.PRIVATE:
                case Modifier.PROTECTED:
                    excluding.addAll(Arrays.asList(Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE));
                    break;
            }
        }
        return mergeModifiers(modifiers);
    }

    /**
     * Generates a random modifier value of modifiers that are applicable
     * to methods (e.g. access modifiers, synchronized).
     *
     * @return a random integer describing field modifiers
     */
    public static int getMethodModifiers() {
        return getModifiers();
    }

    /**
     * Generates a random modifier value of modifiers that are applicable
     * to fields (e.g. access modifiers, static, final).
     *
     * @return a random integer describing field modifiers
     */
    public static int getFieldModifiers() {
        return getModifiers(Modifier.SYNCHRONIZED);
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
