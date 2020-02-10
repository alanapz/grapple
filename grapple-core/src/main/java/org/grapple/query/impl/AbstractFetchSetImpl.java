package org.grapple.query.impl;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static org.grapple.query.impl.EntityOrderByImpl.entityOrderBy;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntitySortKey;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryField;
import org.grapple.query.SortDirection;

abstract class AbstractFetchSetImpl<X> implements FetchSet<X> {

    private final Set<EntityField<X, ?>> selections = new LinkedHashSet<>();

    private final Set<EntityFilter<X>> filters = new LinkedHashSet<>();

    private final Map<EntityJoin<X, ?>, FetchSet<?>> joins = new LinkedHashMap<>();

    @Override
    public FetchSet<X> select(EntityField<X, ?> selection) {
        requireNonNull(selection, "selection");
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
    public <Y> FetchSet<Y> getJoin(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        return (FetchSet<Y>) joins.get(join);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Y> FetchSet<Y> join(EntityJoin<X, Y> join) {
        requireNonNull(join, "join");
        final FetchSet<Y> existing = (FetchSet<Y>) joins.get(join);
        if (existing != null) {
            return existing;
        }
        final FetchSet<Y> joinedFetchSet = new FetchSetImpl<>(fetchRoot(), this, join);
        joins.put(join, joinedFetchSet);
        return joinedFetchSet;
    }

    @Override
    public <Y> FetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
        requireNonNull(join, "join");
        org.grapple.utils.Utils.apply(join(join), consumer);
        return this;
    }

    @Override
    public FetchSet<X> filter(EntityFilter<X> filter) {
        requireNonNull(filter, "filter");
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
    public FetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction) {
        requireNonNull(field, "field");
        requireNonNull(direction, "direction");
        fetchRoot().orderBy.add(entityOrderBy(this, direction, field));
        return this;
    }

    @Override
    public FetchSet<X> orderBy(EntitySortKey<X> sortKey, SortDirection direction) {
        requireNonNull(sortKey, "sortKey");
        requireNonNull(direction, "direction");
        fetchRoot().orderBy.add(entityOrderBy(this, direction, sortKey));
        return this;
    }

    @Override
    public FetchSet<X> apply(Consumer<FetchSet<X>> consumer) {
        return org.grapple.utils.Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<FetchSet<X>, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }

    protected abstract RootFetchSetImpl<?> fetchRoot();
}
