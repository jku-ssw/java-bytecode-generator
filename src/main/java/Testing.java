import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.NotFoundException;
import utils.ClazzFileContainer;

public class Testing {

    public static void main(String[] args) throws NotFoundException, CannotCompileException {
        ClazzFileContainer container = new ClazzFileContainer("file");
        CtMethod main = container.getClazzFile().getDeclaredMethod("main");
        System.out.println("switch(3){}");
        main.insertAfter("switch(3){case 1:}");
        switch (1) {
        }
        switch(3){}

        switch ((byte) 3) {
            case (byte) 1:
                System.out.println(false);
            case (byte) 2:
                System.out.println(true);
        }

    }
}


//[{adsjfkladsjfljad,akjsdfa}, {adsjfkladsjfljad,akjsdfa}]

