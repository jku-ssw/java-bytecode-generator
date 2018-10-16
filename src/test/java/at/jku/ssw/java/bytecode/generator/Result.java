package at.jku.ssw.java.bytecode.generator;

public final class Result {
    public final String className;
    public final String out;
    public final String err;

    public Result(String className, String out, String err) {
        this.className = className;
        this.out = out;
        this.err = err;
    }
}
