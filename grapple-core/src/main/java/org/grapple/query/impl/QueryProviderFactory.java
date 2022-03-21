package org.grapple.query.impl;

import static java.util.function.Function.identity;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.EntityManager;

import org.grapple.core.Chainable;
import org.grapple.query.QueryBuilder;
import org.grapple.query.QueryProvider;
import org.grapple.query.RootFetchSet;
import org.grapple.utils.Utils;

import org.jetbrains.annotations.NotNull;

public final class QueryProviderFactory {

    private QueryProviderFactory() {

    }

    public static QueryProvider buildQueryProvider(@NotNull Consumer<QueryProviderConfig> configConsumer) {
        final QueryProviderConfig config = new QueryProviderConfig().apply(configConsumer).copy();
        if (config.entityManager == null) {
            throw new IllegalArgumentException("entityManager required");
        }
        if (config.queryBuilder == null) {
            throw new IllegalArgumentException("queryBuilder required");
        }
        return new QueryProviderImpl() {

            @Override
            public EntityManager getEntityManager() {
                return config.entityManager.get();
            }

            @Override
            public QueryBuilder getQueryBuilder(QueryBuilder source) {
                return config.queryBuilder.apply(source);
            }

            @Override
            public <X> RootFetchSet<X> newQuery(@NotNull Class<X> entityClass) {
                return new RootFetchSetImpl<>(this, entityClass);
            }
        };
    }

    public static final class QueryProviderConfig implements Chainable<QueryProviderConfig> {

        Supplier<EntityManager> entityManager;

        Function<QueryBuilder, QueryBuilder> queryBuilder = identity();

        public QueryProviderConfig setEntityManager(@NotNull Supplier<EntityManager> entityManager) {
            this.entityManager = entityManager;
            return this;
        }

        public QueryProviderConfig setQueryBuilder(@NotNull Function<QueryBuilder, QueryBuilder> queryBuilder) {
            this.queryBuilder = queryBuilder;
            return this;
        }

        QueryProviderConfig copy() {
            return new QueryProviderConfig()
                    .setEntityManager(entityManager)
                    .setQueryBuilder(queryBuilder);
        }
    }

}
