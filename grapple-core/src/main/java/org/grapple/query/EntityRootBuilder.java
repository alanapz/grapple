package org.grapple.query;

import static java.util.Objects.requireNonNull;

public final class EntityRootBuilder {

    private EntityRootBuilder() {

    }

    public static <T> EntityRoot<T> from(Class<T> entityClass) {
        requireNonNull(entityClass, "entityClass");
        return new EntityRoot<T>() {

            @Override
            public Class<T> getEntityClass() {
                return entityClass;
            }

            @Override
            public EntityFilter<T> getFilter() {
                return Filters.alwaysTrue();
            }
        };
    }

    public static <T> EntityRoot<T> from(Class<T> entityClass, EntityFilter<T> filter) {
        requireNonNull(entityClass, "entityClass");
        requireNonNull(filter, "filter");
        return new EntityRoot<T>() {

            @Override
            public Class<T> getEntityClass() {
                return entityClass;
            }

            @Override
            public EntityFilter<T> getFilter() {
                return filter;
            }
        };
    }
}
