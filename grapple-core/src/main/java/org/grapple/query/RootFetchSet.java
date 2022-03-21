package org.grapple.query;

import java.util.List;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;

public interface RootFetchSet<X> extends FetchSet<X> {

    Class<X> getEntityClass();

    int getFirstResult();

    RootFetchSet<X> setFirstResult(int firstResult);

    int getMaxResults();

    RootFetchSet<X> setMaxResults(int maxResults);

    List<EntityOrderBy<?>> getOrderBy();

    <T> RootFetchSet<X> setQueryParameter(QueryParameter<T> parameter, T value);

    QueryResultList<X> execute(EntityManager entityManager, EntityRoot<X> entityRoot);

    EntityResultList<X> entityQuery(EntityManager entityManager, EntityRoot<X> entityRoot);

    @Override
    RootFetchSet<X> select(@NotNull EntityField<X, ?> selection);

    @Override
    <Y> RootFetchSet<X> join(@NotNull EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer);

    @Override
    RootFetchSet<X> filter(@NotNull EntityFilter<X> filter);

    @Override
    RootFetchSet<X> orderBy(@NotNull QueryField<X, ?> field, @NotNull SortDirection direction);

    @Override
    RootFetchSet<X> orderBy(@NotNull EntitySortKey<X> field, @NotNull SortDirection direction);

    @Override
    RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer); // Shame we can't type it as Consumer<RootFetchSet>
}
