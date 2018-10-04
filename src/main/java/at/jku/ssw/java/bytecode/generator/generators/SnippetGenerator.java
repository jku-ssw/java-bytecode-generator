package at.jku.ssw.java.bytecode.generator.generators;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;

import static at.jku.ssw.java.bytecode.generator.utils.Gen.*;

public class SnippetGenerator extends Generator {

    public SnippetGenerator(RandomCodeGenerator codeGenerator) {
        super(codeGenerator.getClazzFileContainer());
    }

    public void generateHashCodeSubtraction(MethodLogger method) {
        if (!method.isStatic()) {
            String src = Statement(
                    SystemOutPrintln(
                            concat(
                                    asStr("hash difference = "),
                                    inPar(subtract(
                                            call("hashCode"),
                                            call("hashCode")
                                    ))
                            )
                    )
            );
            insertIntoMethodBody(method, src);
        }
    }
}
