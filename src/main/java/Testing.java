import generator.FieldAndVarGenerator;
import javassist.*;
import utils.Field;
import utils.FieldType;


public class Testing {

    public static void main(String[] args)  {
        System.out.println(args.length == 0);

        //TODO handle UserInput Parameters: Probabilities, filename, optionals

        FieldAndVarGenerator fld_generator = new FieldAndVarGenerator("MyTestClazz");

        //generate global field
        int[] modifier = {Modifier.STATIC, Modifier.FINAL};
        boolean gf = fld_generator.generateField("x", FieldType.boolean_, true, modifier);
        boolean sf = fld_generator.generateField("z", FieldType.string_, "Hallo", modifier);
        boolean sf2 = fld_generator.generateField("a", FieldType.string_, null, modifier);

        //generate local field
        boolean lf = fld_generator.generateVariableInMain("y", FieldType.int_, 3);

        fld_generator.generatePrintFieldStatement("x");
        fld_generator.generatePrintFieldStatement("z");
        fld_generator.generatePrintFieldStatement("a");
        fld_generator.generatePrintVariableStatement("y", "main");

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
//TODO randomize file with FieldAndVarGenerator

