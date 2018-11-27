package at.jku.ssw.java.bytecode.generator.metamodel.builders;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static at.jku.ssw.java.bytecode.generator.types.TypeCache.CACHE;

/**
 * Represents an inferred wrapper around a library method
 * (e.g. {@link String#valueOf(int)}).
 *
 * @param <T> The return type
 */
public final class LibMethod<T> implements MethodBuilder<T> {

    public static final List<String> EXCLUDED = Arrays.asList(
            "public boolean java.lang.Object.equals(java.lang.Object)",
            "public native int java.lang.Object.hashCode()",
            "public final void java.lang.Object.wait() throws java.lang.InterruptedException",
            "public final native void java.lang.Object.wait(long) throws java.lang.InterruptedException",
            "public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException",
            "public java.lang.String java.lang.Object.toString()",
            "public final native void java.lang.Object.notify()",
            "public final native void java.lang.Object.notifyAll()",
            "public final native java.lang.Class java.lang.Object.getClass()"
    );

    /**
     * The method's sender (either the class for static methods or the instance
     * for instance methods).
     */
    private final RefType<?> sender;

    /**
     * The name of the method.
     */
    private final String name;

    /**
     * The modifiers of this method.
     */
    private final int modifiers;

    /**
     * The parameter types.
     */
    private final List<MetaType<?>> argumentTypes;

    /**
     * The return type.
     */
    private final MetaType<T> returns;

    /**
     * Creates a new library method.
     *
     * @param sender        The sender / class instance that invokes
     *                      this method
     * @param name          The name of this method
     * @param modifiers     The method modifiers
     * @param argumentTypes The argument types
     * @param returns       The return type
     */
    private LibMethod(RefType<?> sender,
                      String name,
                      int modifiers,
                      List<MetaType<?>> argumentTypes,
                      MetaType<T> returns) {
        this.sender = sender;
        this.name = name;
        this.modifiers = modifiers;
        this.argumentTypes = argumentTypes;
        this.returns = returns;
    }

    /**
     * Infers a new {@link LibMethod} instance from the given reflective
     * method.
     *
     * @param method The reflective method
     * @return a new method logger which encapsulates the reflective method;
     * nothing if an involved type cannot be inferred
     */
    @SuppressWarnings("unchecked")
    public static Optional<LibMethod<?>> infer(Method method) {
        // do not attempt to infer method if it is excluded anyway
        if (EXCLUDED.contains(method.toString()))
            return Optional.empty();

        // look up declaring class ("container" / "sender")
        return CACHE.find(method.getDeclaringClass()).flatMap(sender ->
                // look up return type
                CACHE.find(method.getReturnType()).map(returnType -> {
                    List<MetaType<?>> paramTypes =
                            Arrays.stream(method.getParameterTypes())
                                    .map(CACHE::find)
                                    .map(t -> t.orElse(null))
                                    .collect(Collectors.toList());

                    if (paramTypes.stream().anyMatch(Objects::isNull))
                        return null;

                    return new LibMethod<>(
                            (RefType<?>) sender,
                            method.getName(),
                            method.getModifiers(),
                            paramTypes,
                            returnType
                    );
                })
        );
    }

    /**
     * Returns a string representation of this object.
     * This implementation parses the actual method signature and returns it.
     */
    @Override
    public String toString() {
        return String.format(
                TO_STRING_FORMAT,
                Modifier.toString(modifiers),
                returns.descriptor(),
                sender,
                name,
                argumentTypes.stream()
                        .map(MetaType::descriptor)
                        .collect(Collectors.joining(", ")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RefType<?> sender() {
        return sender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int modifiers() {
        return modifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MetaType<?>> argumentTypes() {
        return argumentTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetaType<T> returns() {
        return returns;
    }
}
