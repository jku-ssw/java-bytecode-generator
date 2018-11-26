package at.jku.ssw.java.bytecode.generator.metamodel.base;

import at.jku.ssw.java.bytecode.generator.logger.FieldVarLogger;
import at.jku.ssw.java.bytecode.generator.metamodel.base.constants.*;
import at.jku.ssw.java.bytecode.generator.metamodel.base.operations.*;
import at.jku.ssw.java.bytecode.generator.utils.ErrorUtils;

/**
 * Generic interface for resolvers that interpret generated expressions.
 *
 * @param <T> The type of result that this resolver produces
 */
public interface Resolver<T> {
    default <U> T resolve(Expression<U> expression) {
        assert expression != null;

        if (expression instanceof Constant)
            return resolve((Constant<U>) expression);
        if (expression instanceof TypeIdentifier)
            return resolve((TypeIdentifier<U>) expression);
        if (expression instanceof ArrayInit)
            return resolve((ArrayInit<U>) expression);
        if (expression instanceof ConstructorCall)
            return resolve((ConstructorCall<U>) expression);
        if (expression instanceof FieldVarLogger)
            return resolve((FieldVarLogger<T>) expression);
        if (expression instanceof MethodCall)
            return resolve((MethodCall<T>) expression);

        throw ErrorUtils.shouldNotReachHere("Unexpected expression " + expression.getClass());
    }

    default <U> T resolve(Constant<U> constant) {
        if (constant instanceof ByteConstant)
            return resolve((ByteConstant) constant);
        if (constant instanceof ShortConstant)
            return resolve((ShortConstant) constant);
        if (constant instanceof IntConstant)
            return resolve((IntConstant) constant);
        if (constant instanceof LongConstant)
            return resolve((LongConstant) constant);
        if (constant instanceof FloatConstant)
            return resolve((FloatConstant) constant);
        if (constant instanceof DoubleConstant)
            return resolve((DoubleConstant) constant);
        if (constant instanceof BooleanConstant)
            return resolve((BooleanConstant) constant);
        if (constant instanceof StringConstant)
            return resolve((StringConstant) constant);
        if (constant instanceof NullConstant)
            return resolve((NullConstant<U>) constant);
        if (constant instanceof CharConstant)
            return resolve((CharConstant) constant);

        throw ErrorUtils.shouldNotReachHere("Unexpected constant " + constant.getClass());
    }

    T resolve(ByteConstant constant);

    T resolve(ShortConstant constant);

    T resolve(IntConstant constant);

    T resolve(LongConstant constant);

    T resolve(FloatConstant constant);

    T resolve(DoubleConstant constant);

    T resolve(BooleanConstant constant);

    T resolve(CharConstant constant);

    T resolve(StringConstant constant);

    <U> T resolve(NullConstant<U> constant);

    <U> T resolve(TypeIdentifier<U> typeIdentifier);

    <U> T resolve(ArrayInit<U> arrayInit);

    <U> T resolve(ConstructorCall<U> constructorCall);

    <U> T resolve(FieldVarLogger<U> fieldVarLogger);

    <U> T resolve(MethodCall<U> methodCall);

}
