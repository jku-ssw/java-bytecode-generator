package generator;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import utils.ClazzFileContainer;

/**
 * generates the smallest possible class-file that is executable
 */
public class Generator {

    ClazzFileContainer clazzContainer;

    /**
     * Takes an existing utils.ClazzFileContainer to exend
     *
     * @param cf
     */
    public Generator(ClazzFileContainer cf) {
        this.clazzContainer = cf;
        createMinExecutableFile();
    }

    /**
     * creates a generator.Generator with a new utils.ClazzFileContainer
     *
     * @param filename
     */
    public Generator(String filename) {
        this.clazzContainer = new ClazzFileContainer(filename);
        createMinExecutableFile();
    }


    /**
     * creates a generator.Generator with a new utils.ClazzFileContainer with a default filename
     */
    public Generator() {
        this.clazzContainer = new ClazzFileContainer("GenClazz");
        createMinExecutableFile();
    }


    private void createMinExecutableFile() {
        try {
            CtMethod m = CtNewMethod.make(
                    "public void main(String[] args) {}",
                    clazzContainer.getClazzFile());
            this.clazzContainer.getClazzFile().addMethod(m);
        } catch (CannotCompileException e) {
            System.err.println("Cannot create minimal executable class-file");
            e.printStackTrace();
        }
    }

    public CtClass getClazzFile() {
        return clazzContainer.getClazzFile();
    }

}

