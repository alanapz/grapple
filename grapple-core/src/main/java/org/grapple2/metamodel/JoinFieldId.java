package org.grapple2.metamodel;

import org.grapple.reflect.TypeLiteral;

public final class JoinFieldId<E, T> {

    private final String name;

    private final Class<E> entityClass;

    private final TypeLiteral<T> resultType;

    private JoinFieldId(String name, Class<E> entityClass, TypeLiteral<T> resultType) {
        this.name = name;
        this.entityClass = entityClass;
        this.resultType = resultType;
    }

    public static <E, T> JoinFieldId<E, T> joinFieldId(String name, Class<E> entityClass, TypeLiteral<T> resultType) {
        return new JoinFieldId<>(name, entityClass, resultType);
    }
}
