package org.grapple.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.jetbrains.annotations.NotNull;

public abstract class GenericLiteral<T> implements TypeLiteral<T>, Comparable<GenericLiteral<T>> {

    protected final Type type;

    protected GenericLiteral() {
        final Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType)) { // sanity check, should never happen
            throw new IllegalArgumentException("Can't construct GenericTypeToken without actual type information");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        if (type instanceof TypeVariable<?>) {
            throw new IllegalArgumentException("Cannot use type variable as type reference");
        }
    }

    @Override
    public final Type getType() {
        return type;
    }

    @Override
    public final boolean isPrimitiveType() {
        // A generic type can never be primitive
        return false;
    }

    @Override
    public final GenericLiteral<T> wrapPrimitiveTypeIfNecessary() {
        // A generic type can never be primitive
        return this;
    }

    @Override
    public final boolean isSubtypeOf(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof GenericLiteral<?>)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return type.equals(((GenericLiteral<?>) other).type);
    }

    @Override
    public final int hashCode() {
        return type.hashCode();
    }

    @Override
    public final String toString() {
        return type.getTypeName();
    }

    @Override
    public final int compareTo(@NotNull GenericLiteral<T> unused) {
        return 0;
    }
}

