package generator;

import javassist.*;

public class ByteCodeGenerator {
    CtClass gc;

    public ByteCodeGenerator(CtClass gc) {
        this.gc = gc;
    }
}
