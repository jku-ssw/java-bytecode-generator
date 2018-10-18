package at.jku.ssw.java.bytecode.generator;

/**
 * Captures the property of a generated class.
 */
public final class GeneratedClass {
    public final String name;
    public final int seed;

    public GeneratedClass(String name, int seed) {
        this.name = name;
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "GeneratedClass{" +
                "name='" + name + '\'' +
                ", seed=" + seed +
                '}';
    }
}
