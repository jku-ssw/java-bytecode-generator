package generators;

import logger.MethodLogger;

import static utils.Gen.*;

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
