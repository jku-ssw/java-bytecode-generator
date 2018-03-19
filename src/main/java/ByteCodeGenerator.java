import generator.FieldGenerator;
import generator.Generator.FieldType;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;


public class ByteCodeGenerator {

    public static void main(String[] args) throws NotFoundException, CannotCompileException {
        //TODO handle UserInput Parameters: Probabilities, filename, optionals

        FieldGenerator fld_generator = new FieldGenerator("MyTestClazz");
        CtClass c = fld_generator.getClazzFile();
        //generate global field
        boolean gf = fld_generator.generateStaticPrimitiveField("x", FieldType.Boolean, false);

        //generate local field
        boolean lf = fld_generator.generateLocalPrimitiveField("y", FieldType.Int, 3);

        //generate global string field
        boolean sf = fld_generator.generateStaticStringField("z", "Hallo");
        boolean sf2 = fld_generator.generateStaticStringField("a", null);

        fld_generator.generateGlobalFieldAccess("z");

        CtMethod main = fld_generator.getClazzFile().getDeclaredMethod("main");
        main.insertAfter("System.out.println(x);");
        main.insertAfter("System.out.println(y);");
        //generate field access

        //write File
        fld_generator.writeFile();

    }
}

//TODO add Default values to Fields
//TODO make simple arithmetik operations and field access
//TODO randomize file with FieldGenerator
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

