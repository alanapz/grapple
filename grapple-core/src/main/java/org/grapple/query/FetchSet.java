package org.grapple.query;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.grapple.utils.Chainable;
import org.grapple.utils.EntitySortKey;

public interface FetchSet<X> extends Chainable<FetchSet<X>> {

    RootFetchSet<?> getFetchRoot();

    FetchSet<?> getFetchParent();

    EntityJoin<?, X> getJoinedBy();

    FetchSet<X> select(EntityField<X, ?> selection);

    Set<EntityField<X, ?>> getSelections();

    FetchSet<X> filter(EntityFilter<X> filter);

    Set<EntityFilter<X>> getFilters();

    <Y> FetchSet<Y> join(EntityJoin<X, Y> join);

    <Y> FetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer);

    <Y> FetchSet<Y> getJoin(EntityJoin<X, Y> join);

    Map<EntityJoin<X, ?>, FetchSet<?>> getJoins();

    FetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction);

    FetchSet<X> orderBy(EntitySortKey<X> field, SortDirection direction);

}
