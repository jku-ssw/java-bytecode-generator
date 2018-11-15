package at.jku.ssw.java.bytecode.generator.metamodel.base;

/**
 * Expression that identifies a type (e.g. for type casts or for a
 * type descriptor).
 *
 * @param <T> The actual Java type that is identified
 */
public interface TypeIdentifier<T> extends Expression<T> {
}
