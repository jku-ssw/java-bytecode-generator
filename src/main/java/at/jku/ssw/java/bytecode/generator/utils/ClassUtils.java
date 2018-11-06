package at.jku.ssw.java.bytecode.generator.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ClassUtils {

    /**
     * Returns the number of dimensions that this type describes.
     *
     * @param type The type
     * @return the number of dimensions that this type describes. Returns 0
     * if this type is no array type
     */
    public static int dimensions(Class<?> type) {
        assert type != null;

        int dim = 0;

        for (Class<?> c = type.getComponentType(); c != null; c = c.getComponentType())
            dim++;

        return dim;
    }

    /**
     * Returns the nth component type within this type.
     *
     * @param n    The number of dimensions
     * @param type The type to analyze
     * @return a class describing the type in the given dimension
     * or nothing if this type is no array type
     */
    public static Optional<Class<?>> nthComponentType(int n, Class<?> type) {
        assert n > 0;

        return IntStream.rangeClosed(0, n)
                .<Optional<Class<?>>>mapToObj(__ -> Optional.of(type))
                .reduce((o, __) -> o.flatMap(
                        c -> Optional.ofNullable(c.getComponentType())))
                .flatMap(Function.identity());
    }
}
