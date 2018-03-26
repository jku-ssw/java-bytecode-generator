import generator.FieldAndVarGenerator;
import javassist.*;
import utils.Field;
import utils.FieldType;


public class Testing {

    public static void main(String[] args)  {

        //TODO handle UserInput Parameters: Probabilities, filename, optionals

        FieldAndVarGenerator fld_generator = new FieldAndVarGenerator("MyTestClazz");

        //generate global field
        int[] modifier = {Modifier.STATIC};
        boolean gf = fld_generator.generateField("x", FieldType.Boolean, true, modifier);
        boolean sf = fld_generator.generateField("z", FieldType.String, "Hallo", modifier);
        boolean sf2 = fld_generator.generateField("a", FieldType.String, null, modifier);

        //generate local field
        boolean lf = fld_generator.generateVariable("y", FieldType.Int, 3, "main");

        fld_generator.setFieldValue("x", false, "main");
        fld_generator.setVariableValue("y", 300, "main");
        fld_generator.setFieldValue("a", "Hey", "main");

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

