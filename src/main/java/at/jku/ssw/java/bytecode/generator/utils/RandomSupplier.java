package at.jku.ssw.java.bytecode.generator.utils;

import at.jku.ssw.java.bytecode.generator.metamodel.base.constants.*;
import at.jku.ssw.java.bytecode.generator.types.TypeCache;
import at.jku.ssw.java.bytecode.generator.types.base.ArrayType;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.RestrictedIntType;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.ArrayType.MIN_ARRAY_DIM_LENGTH;
import static at.jku.ssw.java.bytecode.generator.types.base.MetaType.Kind;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.BYTE;
import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.SHORT;
import static at.jku.ssw.java.bytecode.generator.types.base.VoidType.VOID;
import static at.jku.ssw.java.bytecode.generator.utils.ErrorUtils.shouldNotReachHere;

public class RandomSupplier {

    private final int maxArrayDim;
    private final int maxArrayDimSize;
    private final int pPrimitives;
    private final int pObjects;
    private final int pArray;
    private final int pVoid;
    private final int pRestrictedArray;

    private int methodCharNum = 97;
    private int varCharNum = 97;
    private int varRepeat = 0;
    private int methodRepeat = 0;

    private final Random rand;
    private final Randomizer randomizer;

    static private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int MAX_STRING_LENGTH = 20;

    private static final List<Integer> MODIFIERS = Arrays.asList(
            Modifier.STATIC,
            Modifier.FINAL,
            Modifier.SYNCHRONIZED,
            Modifier.PUBLIC,
            Modifier.PRIVATE,
            Modifier.PROTECTED
    );

    public RandomSupplier(Random rand, int maxArrayDim, int maxArrayDimSize, int pPrimitives, int pObjects, int pArray, int pVoid, int pRestrictedArray) {
        this.rand = rand;
        this.randomizer = new Randomizer(rand);

        this.maxArrayDim = maxArrayDim;
        this.maxArrayDimSize = maxArrayDimSize;
        this.pPrimitives = pPrimitives;
        this.pObjects = pObjects;
        this.pArray = pArray;
        this.pVoid = pVoid;
        this.pRestrictedArray = pRestrictedArray;
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

    public MetaType<?> primitiveType() {
        return randomizer.oneOf(TypeCache.CACHE.primitiveTypes())
                .orElseThrow(() -> new AssertionError("No primitive types available"));
    }

    public MetaType<?> classType() {
        return randomizer.oneOf(TypeCache.CACHE.refTypes())
                .orElseThrow(() -> new AssertionError("No class types available"));
    }

    public MetaType<?> arrayType(int dim) {
        return randomizer
                .oneOf(TypeCache.CACHE.types().filter(t -> t.kind() != Kind.VOID))
                .map(t -> ArrayType.of(t, dim))
                .orElseThrow(() -> new AssertionError("Could not create array type"));
    }

    public MetaType<?> restrictedArrayType(int dim) {
        return randomizer
                .oneOf(TypeCache.CACHE.types().filter(t -> t.kind() != Kind.VOID))
                .map(t -> ArrayType.of(t, dim, arrayRestriction(dim)))
                .orElseThrow(() -> new AssertionError("Could not create array type"));
    }

    public BitSet[] arrayRestriction(int dim) {
        return IntStream.range(0, dim)
                .mapToObj(__ ->
                        rand.ints(0, MIN_ARRAY_DIM_LENGTH)
                                .limit(rand.nextInt(MIN_ARRAY_DIM_LENGTH))
                                .boxed()
                                .reduce(
                                        new BitSet(),
                                        (b, s) -> {
                                            b.set(s);
                                            return b;
                                        }, (b1, b2) -> {
                                            b1.and(b2);
                                            return b1;
                                        }))
                .toArray(BitSet[]::new);
    }

    /**
     * Returns a random type.
     * This can either be a primitive type, one of the registered instance
     * types or any creatable array type.
     * Void is not included.
     *
     * @return a random type
     */
    public MetaType<?> type() {
        return randomizer.withProbabilities(
                new int[]{pPrimitives, pObjects, pArray, pRestrictedArray},
                this::primitiveType,
                this::classType,
                () -> arrayType(rand.nextInt(maxArrayDim) + 1),
                () -> restrictedArrayType(rand.nextInt(maxArrayDim) + 1)
        ).orElseThrow(() -> new AssertionError("Could not fetch random type"));
    }

    /**
     * Returns an infinite stream that is generated by randomly picking a
     * certain type kind and generating / selecting a type of this kind.
     *
     * @return an infinite stream of randomly selected {@link MetaType}
     * instances.
     */
    public Stream<MetaType<?>> types() {
        return Stream.generate(this::type);
    }

    /**
     * Returns a random return type.
     * This can be any primitive type, as well as one of the registered
     * class types, any creatable array type or void.
     *
     * @return a random return type
     */
    public MetaType<?> returnType() {
        return randomizer.withProbabilities(
                new int[]{pVoid, 100 - pVoid},
                () -> VOID,
                this::type
        ).orElseThrow(() -> new AssertionError("Could not fetch random return type"));
    }

    /**
     * Returns a random constant that corresponds to the given type.
     * If the type describes a reference type (or array), a
     * {@link NullConstant} is returned.
     *
     * @param type The meta type
     * @param <T>  The actual Java class
     * @return a constant expression of the given type
     */
    @SuppressWarnings("unchecked")
    public <T> Constant<T> constantOf(MetaType<T> type) {
        switch (type.kind()) {
            case BYTE:
                return (Constant<T>) new ByteConstant((byte) rand.nextInt());
            case SHORT:
                return (Constant<T>) new ShortConstant((short) rand.nextInt());
            case INT:
                return (Constant<T>) new IntConstant(rand.nextInt());
            case LONG:
                return (Constant<T>) new LongConstant(rand.nextLong());
            case FLOAT:
                return (Constant<T>) new FloatConstant(rand.nextFloat());
            case DOUBLE:
                return (Constant<T>) new DoubleConstant(rand.nextDouble());
            case BOOLEAN:
                return (Constant<T>) new BooleanConstant(rand.nextBoolean());
            case CHAR:
                return (Constant<T>) new CharConstant(CHARACTERS.charAt(rand.nextInt(CHARACTERS.length())));
            case RINT:
                RestrictedIntType rInt = (RestrictedIntType) type;

                IntRange range = rInt.getRange();

                IntStream values = range != null
                        ? rand.ints(0, Integer.min(range.max, maxArrayDimSize + 1))
                        .map(i -> i + range.min)
                        : rInt.getInclusions().stream()
                        .mapToInt(Integer::intValue)
                        .skip(rand.nextInt(rInt.getInclusions().size()));

                return (Constant<T>) new IntConstant(
                        values
                                .filter(rInt::isValid)
                                .findFirst()
                                .orElseThrow(() ->
                                        shouldNotReachHere("Could not find integer constant for restricted type " + type)));
            case INSTANCE:
            case ARRAY:
                return new NullConstant<>((RefType<T>) type);
        }

        throw shouldNotReachHere("Unexpected constant request for type " + type);
    }

    public String getRandomNumericValue(MetaType<?> type, boolean notZero) {
        switch (type.kind()) {
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
                char c = CHARACTERS.charAt(rand.nextInt(CHARACTERS.length()));
                if (notZero) {
                    return "\'" + (c != 0 ? c : ++c) + "\'";
                } else {
                    return "\'" + c + "\'";
                }
            default:
                throw new AssertionError();
        }
    }

    public String getString() {
        int length = rand.nextInt(MAX_STRING_LENGTH + 1);
        return getStringOfLength(length);
    }

    public String getStringOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(rand.nextInt(CHARACTERS.length())));
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

    public Random getRandom() {
        return rand;
    }

}
