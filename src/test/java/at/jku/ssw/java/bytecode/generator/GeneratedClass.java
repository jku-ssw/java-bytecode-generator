package at.jku.ssw.java.bytecode.generator;

/**
 * Captures the property of a generated class.
 */
public final class GeneratedClass {
    public final String name;
    public final int seed;
    public final String args;

    public GeneratedClass(String name, int seed, String args) {
        this.name = name;
        this.seed = seed;
        this.args = args;
    }

    @Override
    public String toString() {
        return "GeneratedClass{" +
                "name='" + name + '\'' +
                ", seed=" + seed +
                ", args='" + args + '\'' +
                '}';
    }
}
