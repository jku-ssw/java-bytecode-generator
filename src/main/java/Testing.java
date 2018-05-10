import javassist.*;
import utils.ClazzFileContainer;

import java.io.IOException;


public class Testing {

    public static void main(String[] args) throws CannotCompileException {
       ClazzFileContainer container = new ClazzFileContainer("MyTestingClazz");
       CtClass myClazz = container.getClazzFile();
        try {
            CtMethod main = myClazz.getDeclaredMethod("main");
            main.insertAfter("if(true){if(true){}}");
            //main.insertAfter("System.out.println(x);");
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        try {
            myClazz.writeFile();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


