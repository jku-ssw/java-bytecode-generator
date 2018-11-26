package at.jku.ssw.java.bytecode.generator.generators.snippets;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.utils.ClazzFileContainer;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;

public class HashCodeSubtraction implements Snippet {
    @Override
    public <T> boolean isPossible(MethodLogger<T> method) {
        return !method.isStatic();
    }

    @Override
    public String generate(RandomSupplier __, ClazzFileContainer ___, MethodLogger ____) {
        return Statement(
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
    }
}
