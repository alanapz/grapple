package org.grapple2.metamodel;

import org.grapple.reflect.TypeLiteral;

public final class ScalarFieldId<E, T> {

    private final String name;

    private final Class<E> entityClass;

    private final TypeLiteral<T> resultType;

    private ScalarFieldId(String name, Class<E> entityClass, TypeLiteral<T> resultType) {
        this.name = name;
        this.entityClass = entityClass;
        this.resultType = resultType;
    }

    public static <E, T> ScalarFieldId<E, T> scalarFieldId(String name, Class<E> entityClass, TypeLiteral<T> resultType) {
        return new ScalarFieldId<>(name, entityClass, resultType);
    }
}
