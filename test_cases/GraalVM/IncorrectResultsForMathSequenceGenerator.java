import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.objectweb.asm.Opcodes.*;

public class IncorrectResultsForMathSequenceGenerator {
    public static void main(String[] args) throws IOException {
        final String className = "IncorrectResultsForMathSequence";
        final String fileName = className + ".class";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", new String[]{});

        final var main = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        main.visitCode();
        main.visitInsn(DCONST_0);
        main.visitInsn(DCONST_0);
        main.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Math.class), "IEEEremainder", "(DD)D", false);
        main.visitInsn(D2F);
        main.visitVarInsn(FSTORE, 1);
        main.visitVarInsn(FLOAD, 1);
        main.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Math.class), "ulp", "(F)F", false);
        main.visitVarInsn(FSTORE, 1);
        main.visitInsn(FCONST_0);
        main.visitVarInsn(FLOAD, 1);
        main.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Math.class), "copySign", "(FF)F", false);
        main.visitVarInsn(FSTORE, 1);
        main.visitFieldInsn(GETSTATIC, Type.getInternalName(System.class), "out", "Ljava/io/PrintStream;");
        main.visitVarInsn(FLOAD, 1);
        main.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "println", "(F)V", false);
        main.visitInsn(RETURN);
        main.visitMaxs(4, 2);
        main.visitEnd();

        Files.write(Paths.get(".").resolve(fileName), cw.toByteArray());
    }
}
