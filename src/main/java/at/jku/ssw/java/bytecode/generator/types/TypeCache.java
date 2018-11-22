package at.jku.ssw.java.bytecode.generator.types;

import at.jku.ssw.java.bytecode.generator.types.base.MetaType;
import at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType;
import at.jku.ssw.java.bytecode.generator.types.base.RefType;
import at.jku.ssw.java.bytecode.generator.types.specializations.BoxedType;
import at.jku.ssw.java.bytecode.generator.types.specializations.DateType;
import at.jku.ssw.java.bytecode.generator.types.specializations.ObjectType;
import at.jku.ssw.java.bytecode.generator.types.specializations.StringType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static at.jku.ssw.java.bytecode.generator.types.base.PrimitiveType.*;

/**
 * Container that keeps track of all registered and available types in the
 * current run.
 */
public enum TypeCache {
    //-------------------------------------------------------------------------
    // region Constants, singleton

    /**
     * Singleton instance.
     */
    CACHE;

    public static final Logger logger = LogManager.getLogger();

    // endregion
    //-------------------------------------------------------------------------
    // region Properties

    /**
     * Captures all primitive types.
     * Since they are initialized on startup, this set remains constant
     * throughout the execution.
     */
    final Set<PrimitiveType<?>> primitiveTypes;

    /**
     * Captures all reference types.
     * Since "array types" are created on demand, this set only stores
     * common reference types (including those that are explicitly covered
     * by a meta type). This set may be modified when new types are registered
     * (e.g. a new class is generated).
     */
    final Set<RefType<?>> refTypes;

    /**
     * Checks whether this cache was already initialized.
     */
    private boolean initialized;

    // endregion
    //-------------------------------------------------------------------------
    // region Initialization

    /**
     * Initializes the type maps.
     * It is done in the constructor as to prevent issues when the classes are
     * loaded (e.g. cross-references, null pointers on pre-initialization
     * access).
     */
    TypeCache() {
        primitiveTypes = new HashSet<>();
        refTypes = new HashSet<>();
        reset();
        System.out.println("INITIALIZED!!!");
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Caching

    /**
     * Registers the given primitive type.
     * This method is private since primitive types are only registered during
     * initialization by the cache itself.
     *
     * @param primitiveType The primitive type
     * @param <T>           The actual primitive type implementation
     * @param <U>           The actual Java class
     * @return the registered primitive type
     */
    private <T extends PrimitiveType<U>, U> T register(T primitiveType) {
        return register(primitiveTypes, primitiveType);
    }

    /**
     * Registers a new reference type.
     *
     * @param type The reference type that should be made available.
     * @param <T>  The actual Java type
     * @return the reference type that was registered (the same as the input)
     */
    public <T extends RefType<U>, U> T register(T type) {
        return register(refTypes, type);
    }

    /**
     * Private helper to register the given type within the given type set.
     *
     * @param types The set of registered types
     * @param type  The new type
     * @param <T>   The actual type of the set values
     * @param <U>   The actual meta type implementation
     * @return the registered type
     */
    private <T extends MetaType<?>, U extends T> U register(Set<T> types, U type) {
        final boolean success = types.add(type);

        assert success : "Type '" + type + "' already registered";
        return type;
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Cache lookup

    /**
     * Checks whether the given primitive type is already cached.
     *
     * @param primitiveType The primitive type
     * @return {@code true} if the primitive type set already contains this
     * primitive type; {@code false} otherwise
     * }
     */
    public boolean contains(PrimitiveType<?> primitiveType) {
        return primitiveTypes.contains(primitiveType);
    }

    /**
     * Checks whether the given reference type is already cached.
     *
     * @param refType The reference type
     * @return {@code true} if the reference type set already contains this
     * reference type; {@code false} otherwise
     */
    public boolean contains(RefType<?> refType) {
        return refTypes.contains(refType);
    }

    /**
     * Concatenates all registered primitive and reference types
     * and returns them as a {@link Stream}.
     *
     * @return a stream of all available (registered) meta types
     */
    public Stream<? extends MetaType<?>> types() {
        return Stream
                .of(primitiveTypes, refTypes)
                .flatMap(Collection::stream);
    }

    /**
     * Returns all registered reference types
     *
     * @return a stream of reference types
     */
    public Stream<RefType<?>> refTypes() {
        return refTypes.stream();
    }

    /**
     * Returns all registered primitive types.
     *
     * @return a stream of primitive types
     */
    public Stream<PrimitiveType<?>> primitiveTypes() {
        return primitiveTypes.stream();
    }

    // endregion
    //-------------------------------------------------------------------------
    // region Cache control

    /**
     * Resets and clears this cache and fills it with the default values.
     */
    public void reset() {
        if (initialized)
            invalidate();
        initialize();
    }

    /**
     * Initializes this cache and fills it with the default values.
     */
    public void initialize() {
        // register primitive types
        register(BYTE);
        register(SHORT);
        register(INT);
        register(LONG);
        register(FLOAT);
        register(DOUBLE);
        register(BOOLEAN);
        register(CHAR);

        // register reference types
        register(ObjectType.OBJECT);
        register(DateType.DATE);
        register(StringType.STRING);

        // register boxed types
        register(BoxedType.BYTE);
        register(BoxedType.SHORT);
        register(BoxedType.INT);
        register(BoxedType.LONG);
        register(BoxedType.FLOAT);
        register(BoxedType.DOUBLE);
        register(BoxedType.BOOLEAN);
        register(BoxedType.CHAR);
        initialized = true;
    }

    /**
     * Invalidates the cache by clearing all entries.
     */
    public void invalidate() {
        primitiveTypes.clear();
        refTypes.clear();
        initialized = false;
    }

    // endregion
    //-------------------------------------------------------------------------
}
