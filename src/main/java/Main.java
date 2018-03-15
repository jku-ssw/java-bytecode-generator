import generator.FieldGenerator;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        //TODO add Filename to User Parameters
        FieldGenerator fld_generator = new FieldGenerator("MyTestClazz");
        CtClass c = fld_generator.getClazzFile();
        try {
            c.writeFile();
        } catch (NotFoundException | IOException | CannotCompileException e) {
            System.err.println("Cannot write class-file");
            e.printStackTrace();
        }
    }
}
//        ClassPool pool = ClassPool.getDefault();
//        CtClass gc = pool.makeClass("GenClazz");
//        generator.FieldGenerator bg = new generator.FieldGenerator(gc);
//        try {
//            bg.generateGlobalPrimitiveField(Modifier.PRIVATE);
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            gc.writeFile();
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//        }
//    }

