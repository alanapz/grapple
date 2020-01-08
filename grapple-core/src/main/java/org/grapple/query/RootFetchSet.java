package org.grapple.query;

import java.util.List;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.grapple.utils.EntitySortKey;

public interface RootFetchSet<X> extends FetchSet<X> {

    int getFirstResult();

    RootFetchSet<X> setFirstResult(int firstResult);

    int getMaxResults();

    RootFetchSet<X> setMaxResults(int maxResults);

    List<EntityOrderBy<?>> getOrderBy();

    QueryResultList execute(EntityManager entityManager, EntityRoot<X> entityRoot);

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
    RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer);

}
