package at.jku.ssw.java.bytecode.generator;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public interface CLIArgumentsProvider extends ArgumentsProvider {

    int repetitions();

    boolean allowArithmeticExceptions();

    int maxLength();

    @Override
    default Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        Random r = new Random();
        Function<Integer, String> zeroTo = i -> String.valueOf(r.nextInt(i));
        Function<Integer, String> oneTo = i -> String.valueOf(r.nextInt(i - 1) + 1);

        return IntStream
                .range(0, repetitions())
                .mapToObj(
                        i -> {
                            List<String> args = new ArrayList<>(Arrays.asList(
                                    "-l", zeroTo.apply(maxLength()),
                                    "-f", zeroTo.apply(30),
                                    "-lv", zeroTo.apply(20),
                                    "-ga", zeroTo.apply(20),
                                    "-la", zeroTo.apply(20),
                                    "-m", zeroTo.apply(100),
                                    "-mc", zeroTo.apply(40),
                                    "-ml", zeroTo.apply(10),
                                    "-mp", zeroTo.apply(10),
                                    "-mo", zeroTo.apply(100),
                                    "-p", "0",
                                    "-jlm", zeroTo.apply(10),
                                    "-cf", "40",
                                    "-cl", "10",
                                    "-cd", "3",
                                    "-mli", zeroTo.apply(6),
                                    "-while", "30",
                                    "-for", "30",
                                    "-dowhile", "20",
                                    "-if", "60",
                                    "-ibf", "10",
                                    "-os", zeroTo.apply(30),
                                    "-as", zeroTo.apply(100),
                                    "-ls", zeroTo.apply(100),
                                    "-bs", zeroTo.apply(100),
                                    "-als", zeroTo.apply(100),
                                    "-abs", zeroTo.apply(100),
                                    "-lbs", zeroTo.apply(100),
                                    "-albs", zeroTo.apply(100),
                                    "-mops", zeroTo.apply(10),
                                    "-snippet", zeroTo.apply(5),
                                    "-break", zeroTo.apply(10),
                                    "-return", zeroTo.apply(10),
                                    "-primitives", oneTo.apply(80),
                                    "-objects", oneTo.apply(40),
                                    "-arrays", oneTo.apply(20),
                                    "-void", oneTo.apply(40),
                                    "-cast", oneTo.apply(20),
                                    "-max_dim", oneTo.apply(3),
                                    "-max_dim_size", zeroTo.apply(100),
                                    "-arrayaccess", zeroTo.apply(50)
                            ));

                            // optionally allow exceptions
                            if (allowArithmeticExceptions() && r.nextBoolean()) {
                                args.add("-of");
                                args.add("-dz");
                            }

                            return arguments(args, i);
                        })
                .limit(repetitions());
    }
}
