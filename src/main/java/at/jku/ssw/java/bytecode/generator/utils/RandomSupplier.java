package at.jku.ssw.java.bytecode.generator.utils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static at.jku.ssw.java.bytecode.generator.utils.FieldVarType.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns.NULL;

public class RandomSupplier {

    public final int maxArrayDim;
    public final int maxArrayDimSize;
    public final int pPrimitives;
    public final int pObjects;
    public final int pArray;
    public final int pVoid;

    private int methodCharNum = 97;
    private int varCharNum = 97;
    private int varRepeat = 0;
    private int methodRepeat = 0;

    private final Random rand;
    private final Randomizer randomizer;

    static private final String STRING_CANDIDATES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_STRING_LENGTH = 20;

    private static final List<Integer> MODIFIERS = Arrays.asList(
            Modifier.STATIC,
            Modifier.FINAL,
            Modifier.SYNCHRONIZED,
            Modifier.PUBLIC,
            Modifier.PRIVATE,
            Modifier.PROTECTED
    );

    public RandomSupplier(Random rand, int maxArrayDim, int maxArrayDimSize, int pPrimitives, int pObjects, int pArray, int pVoid) {
        this.rand = rand;
        this.randomizer = new Randomizer(rand);

        this.maxArrayDim = maxArrayDim;
        this.maxArrayDimSize = maxArrayDimSize;
        this.pPrimitives = pPrimitives;
        this.pObjects = pObjects;
        this.pArray = pArray;
        this.pVoid = pVoid;
    }

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

    public FieldVarType<?> primitiveType() {
        return randomizer.oneOf(FieldVarType.primitiveTypes())
                .orElseThrow(() -> new AssertionError("No primitive types available"));
    }

    public FieldVarType<?> classType() {
        return randomizer.oneOf(FieldVarType.classTypes())
                .orElseThrow(() -> new AssertionError("No class types available"));
    }

    public FieldVarType<?> arrayType(int dim) {
        return randomizer
                .oneOf(FieldVarType.types().filter(t -> t.kind != Kind.VOID))
                .map(t -> FieldVarType.arrayTypeOf(t, dim))
                .orElseThrow(() -> new AssertionError("Could not create array type"));
    }

    /**
     * Returns a random type.
     * This can either be a primitive type, one of the registered instance
     * types or any creatable array type.
     * Void is not included.
     *
     * @return a random type
     */
    public FieldVarType<?> type() {
        return randomizer.withProbabilities(
                new int[]{pPrimitives, pObjects, pArray},
                this::primitiveType,
                this::classType,
                () -> arrayType(rand.nextInt(maxArrayDim) + 1)
        ).orElseThrow(() -> new AssertionError("Could not fetch random type"));
    }

    /**
     * Returns a random return type.
     * This can be any primitive type, as well as one of the registered
     * class types, any creatable array type or void.
     *
     * @return a random return type
     */
    public FieldVarType<?> returnType() {
        return randomizer.withProbabilities(
                new int[]{pVoid, 100 - pVoid},
                () -> VOID,
                this::type
        ).orElseThrow(() -> new AssertionError("Could not fetch random return type"));
    }

    public String castedValue(FieldVarType<?> type) {
        switch (type.kind) {
            case INSTANCE:
            case ARRAY:
                // 25% chance for objects to be initialized with null
                if (rand.nextInt(4) == 0) {
                    // add cast to signal to prevent ambiguous method calls
                    // e.g. `foo(null)` could invoke
                    // `foo(java.lang.String)` or `foo(java.lang.Object)`
                    return cast(NULL).to(type.clazz);
                }
        }

        return castedValueNotNull(type);
    }

    public String castedValueNotNull(FieldVarType<?> type) {
        switch (type.kind) {
            case BYTE:
                return cast((byte) rand.nextInt()).to(byte.class);
            case SHORT:
                return cast((short) rand.nextInt()).to(short.class);
            case INT:
                return String.valueOf(rand.nextInt());
            case LONG:
                return rand.nextLong() + "L";
            case FLOAT:
                return rand.nextFloat() + "f";
            case DOUBLE:
                return rand.nextDouble() + "d";
            case BOOLEAN:
                return String.valueOf(rand.nextBoolean());
            case CHAR:
                return asChar(STRING_CANDIDATES.charAt(rand.nextInt(STRING_CANDIDATES.length())));
            case INSTANCE:
                if (type.clazz.equals(String.class)) {
                    return asStr(getString());
                } else if (type.clazz.equals(Date.class)) {
                    return New(Date.class, rand.nextLong() + "L");
                }
            case ARRAY:
                return NewArray(
                        type.clazz,
                        rand.ints(0, maxArrayDimSize + 1)
                                .map(i -> i + FieldVarType.MIN_ARRAY_DIM_LENGTH)
                                .limit(type.dim)
                                .boxed()
                                .collect(Collectors.toList())
                );
            default:
                throw new java.lang.AssertionError();
        }
    }

    public String getRandomNumericValue(FieldVarType<?> type, boolean notZero) {
        switch (type.kind) {
            case BYTE:
            case SHORT:
            case INT:
                int i = rand.nextInt();
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
                long l = rand.nextLong();
                if (notZero) {
                    return "" + (l != 0L ? l : ++l) + "L";
                } else {
                    return "" + l + "L";
                }
            case FLOAT:
                float f = rand.nextFloat();
                if (notZero) {
                    return "" + (f != 0f ? f : ++f) + "f";
                } else {
                    return "" + f + "f";
                }
            case DOUBLE:
                double d = rand.nextDouble();
                if (notZero) {
                    return "" + (d != 0d ? d : ++d) + "d";
                } else {
                    return "" + d + "d";
                }
            case CHAR:
                char c = STRING_CANDIDATES.charAt(rand.nextInt(STRING_CANDIDATES.length()));
                if (notZero) {
                    return "\'" + (c != 0 ? c : ++c) + "\'";
                } else {
                    return "\'" + c + "\'";
                }
            default:
                throw new java.lang.AssertionError();
        }
    }

    public String getString() {
        int length = rand.nextInt(MAX_STRING_LENGTH + 1);
        return getStringOfLength(length);
    }

    public String getStringOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(STRING_CANDIDATES.charAt(rand.nextInt(STRING_CANDIDATES.length())));
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
    private Optional<Integer> getRandomModifier(Set<Integer> exclusions) {
        List<Integer> possibleModifiers = MODIFIERS.stream()
                .filter(m -> !exclusions.contains(m))
                .collect(Collectors.toList());

        if (possibleModifiers.isEmpty())
            return Optional.empty();

        final int maxRange = possibleModifiers.size() - 1;

        if (maxRange == 0)
            return possibleModifiers.stream().findFirst();

        return possibleModifiers.stream()
                .skip(rand.nextInt(maxRange))
                .findFirst();
    }

    /**
     * Generates a random modifier value.
     *
     * @param exclusions Excluded modifiers
     * @return a random integer describing modifiers
     */
    int getModifiers(int... exclusions) {
        int numberOfModifiers = rand.nextInt(4);
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
    public int getMethodModifiers() {
        return getModifiers();
    }

    /**
     * Generates a random modifier value of modifiers that are applicable
     * to fields (e.g. access modifiers, static, final).
     *
     * @return a random integer describing field modifiers
     */
    public int getFieldModifiers() {
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
