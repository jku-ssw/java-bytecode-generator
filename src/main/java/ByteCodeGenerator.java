import generator.FieldGenerator;
import javassist.*;
import utils.Field;
import utils.FieldType;


public class ByteCodeGenerator {


    public static void main(String[] args) throws NotFoundException, CannotCompileException {

        //TODO handle UserInput Parameters: Probabilities, filename, optionals

        FieldGenerator fld_generator = new FieldGenerator("MyTestClazz");

        //generate global field
        int[] modifier = {Modifier.STATIC, Modifier.FINAL};
        boolean gf = fld_generator.generateGlobalField("x", FieldType.boolean_, true, modifier);
        boolean sf = fld_generator.generateGlobalField("z", FieldType.string_, "Hallo", modifier);
        boolean sf2 = fld_generator.generateGlobalField("a", FieldType.string_, null, modifier);

        //generate local field
        boolean lf = fld_generator.generateLocalFieldInMain("y", FieldType.int_, 3);

        fld_generator.getMain().insertAfter("System.out.println(x);");
        fld_generator.getMain().insertAfter("System.out.println(y);");

        //generate field access

        //write File
        fld_generator.writeFile();

        for(Field f: fld_generator.getClazzContainer().getClazzLogger().getGlobals()) {
            System.out.println(f.getName());
        }

        for(Field f: fld_generator.getClazzContainer().getClazzLogger().getLocals("main")) {
            System.out.println(f.getName());
        }

    }
}

//TODO make simple arithmetik operations and field access
//TODO randomize file with FieldGenerator

