package at.jku.ssw.java.bytecode.generator;

import javassist.ClassPool;
import javassist.bytecode.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.junit.jupiter.api.Assertions.*;

public class BytecodeComparator {

    private static ClassFile getClassFile(byte[] bytecode) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytecode)) {
            return ClassPool.getDefault().makeClass(in).getClassFile();
        }
    }

    public static void assertSameStructure(byte[] expected, byte[] actual) throws Exception {
        ClassFile exp = getClassFile(expected);
        ClassFile act = getClassFile(actual);

        assertThat(act.getAccessFlags(), is(exp.getAccessFlags()));

        List<FieldInfo> expFields = exp.getFields();
        List<FieldInfo> actFields = act.getFields();

        assertThat(actFields.size(), is(expFields.size()));

        assertAll(
                IntStream.range(0, expFields.size())
                        .mapToObj(i -> () -> {
                            FieldInfo expField = expFields.get(i);
                            FieldInfo actField = actFields.get(i);

                            assertThat(
                                    actField.getAccessFlags(),
                                    is(expField.getAccessFlags())
                            );
                            assertThat(
                                    actField.getConstantValue(),
                                    is(expField.getConstantValue())
                            );
                            assertThat(
                                    actField.getDescriptor(),
                                    is(expField.getDescriptor())
                            );
                            assertThat(
                                    actField.getName(),
                                    is(expField.getName())
                            );
                        }));

        List<MethodInfo> expMeths = exp.getMethods();
        List<MethodInfo> actMeths = act.getMethods();

        assertThat(actMeths.size(), is(expMeths.size()));

        assertAll(
                IntStream.range(0, expMeths.size())
                        .mapToObj(i -> () -> {
                            MethodInfo expMeth = expMeths.get(i);
                            MethodInfo actMeth = actMeths.get(i);

                            assertThat(
                                    actMeth.getAccessFlags(),
                                    is(expMeth.getAccessFlags())
                            );
                            assertThat(
                                    actMeth.isConstructor(),
                                    is(expMeth.isConstructor())
                            );
                            assertThat(
                                    actMeth.isStaticInitializer(),
                                    is(expMeth.isStaticInitializer())
                            );
                            assertThat(
                                    actMeth.isMethod(),
                                    is(expMeth.isMethod())
                            );
                            assertThat(
                                    actMeth.getDescriptor(),
                                    is(expMeth.getDescriptor())
                            );
                            assertThat(
                                    actMeth.getName(),
                                    is(expMeth.getName())
                            );
                            ExceptionsAttribute expExAttr = expMeth.getExceptionsAttribute();
                            ExceptionsAttribute actExAttr = actMeth.getExceptionsAttribute();

                            if (expExAttr == null)
                                assertThat(actExAttr, is((ExceptionsAttribute) null));
                            else
                                assertThat(actExAttr.getExceptions(), arrayContaining(expExAttr.getExceptions()));

                            CodeAttribute expCA = expMeth.getCodeAttribute();
                            CodeAttribute actCA = actMeth.getCodeAttribute();

                            CodeIterator expIt = expCA.iterator();
                            CodeIterator actIt = actCA.iterator();

                            while (expIt.hasNext()) {
                                assertTrue(actIt.hasNext());

                                int expI = expIt.next();
                                int actI = actIt.next();

                                assertThat(
                                        actIt.byteAt(actI),
                                        is(expIt.byteAt(expI))
                                );
                            }
                            assertFalse(actIt.hasNext());
                        }));

        String[] expInterfaces = exp.getInterfaces();
        String[] actInterfaces = act.getInterfaces();

        assertThat(actInterfaces.length, is(expInterfaces.length));

        assertAll(
                IntStream.range(0, expInterfaces.length)
                        .mapToObj(i ->
                                () -> assertThat(
                                        actInterfaces[i],
                                        is(expInterfaces[i]))));
    }
}
