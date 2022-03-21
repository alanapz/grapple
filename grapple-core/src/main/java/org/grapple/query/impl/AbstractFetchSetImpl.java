package org.grapple.query.impl;

import static java.util.Collections.unmodifiableSet;
import static org.grapple.query.impl.EntityOrderByImpl.entityOrderBy;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntitySortKey;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryField;
import org.grapple.query.SortDirection;
import org.jetbrains.annotations.NotNull;

abstract class AbstractFetchSetImpl<X> implements FetchSet<X> {

    private final Set<EntityField<X, ?>> selections = new LinkedHashSet<>();

    private final Set<EntityFilter<X>> filters = new LinkedHashSet<>();

    private final Map<EntityJoin<X, ?>, FetchSet<?>> joins = new LinkedHashMap<>();

    @Override
    public FetchSet<X> select(@NotNull EntityField<X, ?> selection) {
        selections.add(selection);
        return this;
    }

    @Override
    public Set<EntityField<X, ?>> getSelections() {
        return unmodifiableSet(selections);
    }

    @Override
    public Map<EntityJoin<X, ?>, FetchSet<?>> getJoins() {
        return readOnlyCopy(joins);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> FetchSet<Y> getJoin(@NotNull EntityJoin<X, Y> join) {
        return (FetchSet<Y>) joins.get(join);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> FetchSet<Y> join(@NotNull EntityJoin<X, Y> join) {
        final FetchSet<Y> existing = (FetchSet<Y>) joins.get(join);
        if (existing != null) {
            return existing;
        }
        final FetchSet<Y> joinedFetchSet = new FetchSetImpl<>(fetchRoot(), this, join);
        joins.put(join, joinedFetchSet);
        return joinedFetchSet;
    }

    @Override
    public <Y> FetchSet<X> join(@NotNull EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
        final FetchSet<Y> joinedFetchSet= join(join);
        if (consumer != null)
        {
            consumer.accept(joinedFetchSet);
        }
        return this;
    }

    @Override
    public FetchSet<X> filter(@NotNull EntityFilter<X> filter) {
        if (!filter.isAlwaysTrue()) {
            filters.add(filter);
        }
        return this;
    }

    @Override
    public Set<EntityFilter<X>> getFilters() {
        return unmodifiableSet(filters);
    }

    @Override
    public FetchSet<X> orderBy(@NotNull QueryField<X, ?> field, @NotNull SortDirection direction) {
        fetchRoot().orderBy.add(entityOrderBy(this, direction, field));
        return this;
    }

    @Override
    public FetchSet<X> orderBy(@NotNull EntitySortKey<X> sortKey, @NotNull SortDirection direction) {
        fetchRoot().orderBy.add(entityOrderBy(this, direction, sortKey));
        return this;
    }

    protected abstract RootFetchSetImpl<?> fetchRoot();
}
