package org.grapple.query;

import java.util.List;
import java.util.function.Consumer;
import javax.persistence.EntityManager;

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
    RootFetchSet<X> select(EntityField<X, ?> selection);

    @Override
    <Y> RootFetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer);

    @Override
    RootFetchSet<X> filter(EntityFilter<X> filter);

    @Override
    RootFetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction);

    @Override
    RootFetchSet<X> orderBy(EntitySortKey<X> field, SortDirection direction);

    @Override
    RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer); // Shame we can't type it as Consumer<RootFetchSet>

}
