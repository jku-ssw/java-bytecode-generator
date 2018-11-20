package at.jku.ssw.java.bytecode.generator.generators.snippets;

import at.jku.ssw.java.bytecode.generator.logger.MethodLogger;
import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;
import at.jku.ssw.java.bytecode.generator.utils.RandomSupplier;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;

/**
 * Generates and prints a check for {@code A.class.isAssignableFrom(B.class)}
 * where {@code A} and {@code B} represent static types.
 */
public class AssignableFromCall implements Snippet {

    @Override
    public <T> boolean isPossible(MethodLogger<T> method) {
        // can be placed anywhere since the types are static
        return true;
    }

    @Override
    public String generate(RandomSupplier randomSupplier) {
        MetaType<?> t1 = randomSupplier.type();
        MetaType<?> t2 = randomSupplier.types()
                .filter(t -> !t.equals(t1))
                .findFirst()
                .orElseThrow(ErrorUtils::shouldNotReachHere);
        String c1 = t1.clazz().getCanonicalName();
        String c2 = t2.clazz().getCanonicalName();

        return Statement(
                SystemOutPrintln(
                        concat(
                                asStr(
                                        String.format(
                                                "%s is assignable from %s: ",
                                                c1,
                                                c2)
                                ),
                                String.format(
                                        "%s.class.isAssignableFrom(%s.class)",
                                        c1,
                                        c2
                                )
                        )
                )
        );
    }
}
