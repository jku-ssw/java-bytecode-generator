package generators;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import logger.ClazzLogger;
import logger.MethodLogger;
import utils.ClazzFileContainer;
import utils.RandomSupplier;

import java.io.*;
import java.util.Random;

abstract class Generator {

    final ClazzFileContainer clazzContainer;
    static final Random RANDOM = new Random();

    public Generator(ClazzFileContainer clazzContainer) {
        this.clazzContainer = clazzContainer;
    }

    public ClazzFileContainer getClazzContainer() {
        return clazzContainer;
    }

    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

    public void writeFile() {
        System.out.println("writing...");
        try {
            InputStream io = new ByteArrayInputStream(this.getClazzFile().toBytecode());
            DataInputStream dis = new DataInputStream(io);
            ClassFile cf = new ClassFile(dis);
            cf.setMajorVersion(52);
            cf.write(new DataOutputStream(new FileOutputStream(this.getClazzFile().getName() + ".class")));
        } catch (IOException | CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public void writeFile(String pathname) {
        try {
            InputStream io = new ByteArrayInputStream(this.getClazzFile().toBytecode());
            DataInputStream dis = new DataInputStream(io);
            ClassFile cf = new ClassFile(dis);
            cf.setMajorVersion(52);
            File file = new File(pathname + "/" + this.getClazzFile().getName() + ".class");
            cf.write(new DataOutputStream(new FileOutputStream(file)));
        } catch (IOException | CannotCompileException e) {
            throw new AssertionError(e);
        }
    }

    public ClazzLogger getClazzLogger() {
        return this.clazzContainer.getClazzLogger();
    }

    public CtMethod getCtMethod(MethodLogger method) {
        try {
            if (method.getName().equals("main")) {
                return this.getClazzFile().getDeclaredMethod(method.getName());
            } else {
                return this.getClazzFile().getDeclaredMethod(method.getName(), method.getCtParamTypes());
            }
        } catch (NotFoundException e) {
            throw new AssertionError(e);
        }
    }

    public RandomSupplier getRandomSupplier() {
        return getClazzContainer().getRandomSupplier();
    }

    void insertIntoMethodBody(MethodLogger method, String src) {
        if (src == null) {
            return;
        }
        try {
            CtMethod ctMethod = this.getCtMethod(method);
            ctMethod.insertAfter(src);
        } catch (CannotCompileException e) {
            throw new AssertionError(e);
        }
    }
}

