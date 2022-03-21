package org.grapple.query;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.grapple.core.Chainable;
import org.jetbrains.annotations.NotNull;

public interface FetchSet<X> extends Chainable<FetchSet<X>> {

    RootFetchSet<?> getFetchRoot();

    FetchSet<?> getFetchParent();

    EntityJoin<?, X> getJoinedBy();

    FetchSet<X> select(@NotNull EntityField<X, ?> selection);

    Set<EntityField<X, ?>> getSelections();

    FetchSet<X> filter(@NotNull EntityFilter<X> filter);

    Set<EntityFilter<X>> getFilters();

    <Y> FetchSet<Y> join(@NotNull EntityJoin<X, Y> join);

    <Y> FetchSet<X> join(@NotNull EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer);

    <Y> FetchSet<Y> getJoin(@NotNull EntityJoin<X, Y> join);

    Map<EntityJoin<X, ?>, FetchSet<?>> getJoins();

    FetchSet<X> orderBy(@NotNull QueryField<X, ?> field, @NotNull SortDirection direction);

    FetchSet<X> orderBy(@NotNull EntitySortKey<X> field, @NotNull SortDirection direction);

}
