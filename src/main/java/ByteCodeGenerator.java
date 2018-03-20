import generator.FieldGenerator;
import utils.FieldType;
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
        boolean gf = fld_generator.generateStaticPrimitiveField("x", FieldType.boolean_, false);

        //generate local field
        boolean lf = fld_generator.generateLocalPrimitiveField("y", FieldType.int_, 3);

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

//TODO make simple arithmetik operations and field access
//TODO randomize file with FieldGenerator

