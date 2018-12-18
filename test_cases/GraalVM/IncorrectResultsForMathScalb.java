import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.objectweb.asm.Opcodes.*;

public class IncorrectResultsForMathScalb {
    public static void main(String[] args) throws IOException {
        final String className = "IncorrectResultsForMathScalb";
        final String fileName = className + ".class";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", new String[]{});

        cw.visitField(ACC_STATIC, "a", "I", null, null);

        final var main = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        final var mainL32 = new Label();
        final var mainL36 = new Label();
        final var mainL41 = new Label();
        final var mainL46 = new Label();

        main.visitCode();
        main.visitIntInsn(SIPUSH, -279);
        main.visitVarInsn(ISTORE, 1);
        main.visitFieldInsn(GETSTATIC, className, "a", "I");
        main.visitJumpInsn(IFLE, mainL36);
        main.visitFieldInsn(GETSTATIC, className, "a", "I");
        main.visitJumpInsn(IFGE, mainL32);
        main.visitInsn(NOP);
        main.visitLabel(mainL32);
        main.visitInsn(ICONST_1);
        main.visitJumpInsn(GOTO, mainL41);
        main.visitLabel(mainL36);
        main.visitInsn(ICONST_0);
        main.visitLabel(mainL41);
        main.visitJumpInsn(IFEQ, mainL46);
        main.visitInsn(ICONST_0);
        main.visitVarInsn(ISTORE, 1);
        main.visitLabel(mainL46);
        main.visitFieldInsn(GETSTATIC, Type.getInternalName(System.class), "out", "Ljava/io/PrintStream;");
        main.visitInsn(ICONST_M1);
        main.visitInsn(I2F);
        main.visitVarInsn(ILOAD, 1);
        main.visitMethodInsn(INVOKESTATIC, Type.getInternalName(Math.class), "scalb", "(FI)F", false);
        main.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "println", "(F)V", false);
        main.visitInsn(RETURN);
        main.visitMaxs(2, 6);
        main.visitEnd();

        Files.write(Paths.get(".").resolve(fileName), cw.toByteArray());
    }
}
