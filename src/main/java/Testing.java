import generator.FieldVarGenerator;
import javassist.*;
import utils.ClazzFileContainer;
import utils.FieldVarType;
import utils.RandomSupplier;

import java.io.IOException;
import java.util.Random;


public class Testing {

    public static void main(String[] args) throws CannotCompileException {
//       ClazzFileContainer container = new ClazzFileContainer("MyTestingClazz");
//       CtClass myClazz = container.getClazzFile();
//        try {
//            CtMethod main = myClazz.getDeclaredMethod("main");
//            main.insertAfter("if(true){if(true){}}");
//            //main.insertAfter("System.out.println(x);");
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        }
//        try {
//            myClazz.writeFile();
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        FieldVarGenerator fvgenerator = new FieldVarGenerator("TestStringException");
//        fvgenerator.generateLocalVariable("x", FieldVarType.String, fvgenerator.getClazzLogger().getMain(), RandomSupplier.getRandomValueAsString(FieldVarType.String));
//        fvgenerator.generateLocalVariable("y", FieldVarType.String, fvgenerator.getClazzLogger().getMain(), RandomSupplier.getRandomValueAsString(FieldVarType.String));
//        fvgenerator.assignVariableToVariable(fvgenerator.getClazzLogger().getMain().getVariable("x"), fvgenerator.getClazzLogger().getMain().getVariable("y"), fvgenerator.getClazzLogger().getMain());
    }
}


