package org.grapple.reflect;

import static java.util.Objects.requireNonNull;

public final class ClassLiteral<T> implements TypeLiteral<T> {

    private final Class<T> clazz;

    private ClassLiteral(Class<T> clazz) {
        this.clazz = requireNonNull(clazz, "clazz");
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    @Override
    public boolean isPrimitiveType() {
        return clazz.isPrimitive();
    }

    @Override
    public final ClassLiteral<T> wrapPrimitiveTypeIfNecessary() {
        return (clazz.isPrimitive() ? new ClassLiteral<>(ReflectUtils.wrapPrimitiveTypeIfNecessary(clazz)) : this);
    }

    @Override
    public boolean isSubtypeOf(Class<?> clazz) {
        return clazz.isAssignableFrom(this.clazz);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ClassLiteral<?>)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        return clazz.equals(((ClassLiteral<?>) other).clazz);
    }

    @Override
    public int hashCode() {
        return clazz.hashCode();
    }

    @Override
    public String toString() {
        return clazz.getName();
    }

    public static <T> ClassLiteral<T> classLiteral(Class<T> clazz) {
        requireNonNull(clazz, "clazz");
        return new ClassLiteral<>(clazz);
    }
}

