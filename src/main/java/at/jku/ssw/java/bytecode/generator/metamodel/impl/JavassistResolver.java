package at.jku.ssw.java.bytecode.generator.metamodel.impl;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ArrayInit;
import at.jku.ssw.java.bytecode.generator.metamodel.base.ConstructorCall;
import at.jku.ssw.java.bytecode.generator.metamodel.base.Resolver;
import at.jku.ssw.java.bytecode.generator.metamodel.base.TypeIdentifier;
import at.jku.ssw.java.bytecode.generator.metamodel.base.constants.*;
import at.jku.ssw.java.bytecode.generator.types.MetaType;

import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Casts.cast;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.*;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns.NULL;
import static at.jku.ssw.java.bytecode.generator.utils.StatementDSL.Patterns.VOID;

/**
 * {@link Resolver} implementation that interprets expressions by
 * forming Javassist strings / expressions (i.e. actual source code).
 */
public class JavassistResolver implements Resolver<String> {

    @Override
    public <T> String resolve(TypeIdentifier<T> typeIdentifier) {
        MetaType<T> type = typeIdentifier.type();

        if (type.isVoid())
            return VOID;

        return type.getClazz().getCanonicalName();
    }

    @Override
    public <T> String resolve(ArrayInit<T> arrayInit) {
        return NewArray(
                arrayInit.type().clazz,
                arrayInit.getParams().stream()
                        .map(this::resolve)
                        .collect(Collectors.toList())
        );
    }

    @Override
    public <U> String resolve(ConstructorCall<U> constructorCall) {
        return New(
                constructorCall.type().clazz,
                constructorCall.getParams().stream()
                        .map(this::resolve)
                        .collect(Collectors.joining())
        );
    }

    @Override
    public <U> String resolve(FieldVarLogger<U> fieldVarLogger) {
        return variable(
                fieldVarLogger.isStatic()
                        ? fieldVarLogger.getClazz()
                        : fieldVarLogger.isField() ? "this" : "",
                fieldVarLogger.name
        );
    }

    @Override
    public String resolve(ByteConstant constant) {
        return cast(constant.raw()).to(byte.class);
    }

    @Override
    public String resolve(ShortConstant constant) {
        return cast(constant.raw()).to(short.class);
    }

    @Override
    public String resolve(IntConstant constant) {
        return String.valueOf(constant.raw());
    }

    @Override
    public String resolve(LongConstant constant) {
        return constant.raw() + "L";
    }

    @Override
    public String resolve(FloatConstant constant) {
        return constant.raw() + "F";
    }

    @Override
    public String resolve(DoubleConstant constant) {
        return constant.raw() + "D";
    }

    @Override
    public String resolve(BooleanConstant constant) {
        return String.valueOf(constant.raw());
    }

    @Override
    public String resolve(StringConstant constant) {
        return constant.value();
    }

    @Override
    public <U> String resolve(NullConstant<U> constant) {
        // add cast to signal to prevent ambiguous method calls
        // e.g. `foo(null)` could invoke
        // `foo(java.lang.String)` or `foo(java.lang.Object)`
        return cast(NULL).to(constant.type().clazz);
    }

    @Override
    public String resolve(CharConstant constant) {
        return asChar(constant.raw());
    }

}
