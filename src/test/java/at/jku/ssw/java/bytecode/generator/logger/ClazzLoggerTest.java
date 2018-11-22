package at.jku.ssw.java.bytecode.generator.logger;

import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClazzLoggerTest {

    private Random rand;
    private RandomSupplier supplier;

    @BeforeEach
    void setUp() {
        rand = new Random();
        supplier = new RandomSupplier(rand, 0, 0, 0, 0, 0, 0, 0);
        CACHE.reset();
    }

    @AfterEach
    void tearDown() {
        rand = null;
        supplier = null;
    }

    @Test
    public void testInstantiation() {
        ClazzLogger c = ClazzLogger.generate(rand, "RegisteredClass", supplier);

        List<RefType<?>> refTypes = CACHE
                .refTypes()
                .collect(Collectors.toList());

        // verify that it is actually registered as reference type
        assertThat(refTypes, hasItem(c));

        // assert that the class can be looked up
        assertTrue(CACHE.contains(c));
    }

    @Test
    public void testDoubleInstantiation() {
        ClazzLogger c1 = ClazzLogger.generate(rand, "AnotherRegisteredClass", supplier);
        ClazzLogger c2 = ClazzLogger.generate(rand, "DuplicateClass", supplier);

        assertThat(c1, is(not(c2)));

        List<RefType<?>> refTypes = CACHE
                .refTypes()
                .collect(Collectors.toList());

        assertThat(refTypes, hasItem(c1));
        assertThat(refTypes, hasItem(c2));

        assertTrue(CACHE.contains(c1));

        assertTrue(CACHE.contains(c2));
    }
}
