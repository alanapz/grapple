package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;

import org.grapple.query.RootFetchSet;

public final class QueryProvider {

    private QueryProvider() {

    }

    public static <X> RootFetchSet<X> newQuery(Class<X> entityClass) {
        requireNonNull(entityClass, "entityClass");
        return new RootFetchSetImpl<>(entityClass);
    }
}
