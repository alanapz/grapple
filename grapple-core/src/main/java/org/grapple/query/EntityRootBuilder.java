package org.grapple.query;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.Filters.alwaysTrue;

public final class EntityRootBuilder {

    private EntityRootBuilder() {

    }

    public static <T> EntityRoot<T> entityRoot(Class<T> entityClass) {
        requireNonNull(entityClass, "entityClass");
        return new EntityRoot<T>() {

            @Override
            public Class<T> getEntityClass() {
                return entityClass;
            }

            @Override
            public EntityFilter<T> getFilter() {
                return (EntityFilter<T>) alwaysTrue();
            }

            @Override
            public String toString() {
                return entityClass.getName();
            }
        };
    }

    public static <T> EntityRoot<T> entityRoot(Class<T> entityClass, EntityFilter<T> filter) {
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

            @Override
            public String toString() {
                return format("%s[%s]", entityClass.getName(), filter);
            }
        };
    }
}
