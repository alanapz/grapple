package org.grapple.reflect;

import java.lang.reflect.Type;

public interface TypeLiteral<T> {

    Type getType();

    boolean isPrimitiveType();

    TypeLiteral<T> wrapPrimitiveTypeIfNecessary();

    boolean isSubtypeOf(Class<?> clazz);
}