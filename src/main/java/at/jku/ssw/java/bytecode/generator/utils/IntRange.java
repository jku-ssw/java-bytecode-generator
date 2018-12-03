package at.jku.ssw.java.bytecode.generator.utils;

import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Helper for integer ranges.
 */
public final class IntRange implements Comparable<IntRange> {
    /**
     * Minimum value.
     */
    public final int min;

    /**
     * Maximum value.
     */
    public final int max;

    /**
     * Creates a new range with the given boundaries.
     *
     * @param min The minimum value
     * @param max The maximum value
     */
    private IntRange(int min, int max) {
        assert max >= min;

        this.min = min;
        this.max = max;
    }

    /**
     * Creates a new inclusive range.
     *
     * @param min The minimum value
     * @param max The maximum value
     * @return a new range that is limited by the given minimum and maximum
     */
    public static IntRange rangeIncl(int min, int max) {
        return new IntRange(min, max);
    }

    /**
     * Creates a new exclusive range.
     *
     * @param llim The lower limit
     * @param ulim The upper limit
     * @return a new range that is limited by the given values (that are not
     * included)
     */
    public static IntRange rangeExcl(int llim, int ulim) {
        return new IntRange(llim + 1, ulim - 1);
    }

    /**
     * Creates a new range with only an upper bound.
     *
     * @param max The maximum value
     * @return a new range that is limited by the given maximum
     */
    public static IntRange rangeTo(int max) {
        return new IntRange(Integer.MIN_VALUE, max);
    }

    /**
     * Creates a new range with only a lower bound.
     *
     * @param min The minimum value
     * @return a new range that is limited by the given minimum
     */
    public static IntRange rangeFrom(int min) {
        return new IntRange(min, Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntRange intRange = (IntRange) o;
        return min == intRange.min &&
                max == intRange.max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + min + ", " + max + "]";
    }

    /**
     * Compares this range with the given range using a lexicographical
     * ordering of the {@link #min} and {@link #max} values.
     *
     * @param o The other range to compare this range with
     * @return {@code 0} if both ranges are equal,
     * a value below zero if this range is before the given range;
     * a value above zero otherwise
     */
    @Override
    public int compareTo(IntRange o) {
        if (this == o)
            return 0;

        if (min != o.min)
            return min - o.min;

        if (max != o.max)
            return max - o.max;

        return 0;
    }

    /**
     * Determines whether the given value is within this range.
     *
     * @param i The integer to check
     * @return {@code true} if this integer is within this range; {@code false}
     * otherwise
     */
    public boolean contains(int i) {
        return i >= min && i <= max;
    }

    /**
     * Determines whether the given range is fully included in this range.
     *
     * @param other The other integer range
     * @return {@code true} if this range includes all values of the given
     * range; {@code false} otherwise
     */
    public boolean contains(IntRange other) {
        return max >= other.max && min <= other.min;
    }

    /**
     * Generates an integer stream that iterates over all values
     * within this range.
     *
     * @return a new (bounded) stream from {@link #min} (inclusive)
     * to {@link #max} (inclusive)
     */
    public IntStream stream() {
        return IntStream.rangeClosed(min, max);
    }
}
