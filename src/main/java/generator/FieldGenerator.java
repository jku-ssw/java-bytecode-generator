package generator;

import javassist.*;
import utils.ClazzFileContainer;

public class FieldGenerator extends Generator {

    public FieldGenerator(String filename) {
        super(filename);
    }

    public FieldGenerator(ClazzFileContainer cf) {
        super(cf);
    }

    public FieldGenerator() {
        super();
    }

    /**
     * generates a global field with given modifier and type
     */
    public void generateGlobalField(int modifier, String name, CtClass type) throws CannotCompileException {
        CtField f = new CtField(type, "name", this.clazzContainer.getClazzFile());
        clazzContainer.getClazzFile().addField(f);
        f.setModifiers(modifier);
    }

}
