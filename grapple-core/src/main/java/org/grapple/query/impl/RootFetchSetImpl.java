package org.grapple.query.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityOrderBy;
import org.grapple.query.EntityResultList;
import org.grapple.query.EntityRoot;
import org.grapple.query.EntitySortKey;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryField;
import org.grapple.query.QueryParameter;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;

import org.jetbrains.annotations.NotNull;

final class RootFetchSetImpl<X> extends AbstractFetchSetImpl<X> implements RootFetchSet<X> {

    private final QueryProviderImpl queryProvider;

    private final Class<X> entityClass;

    private int firstResult = 0;

    private int maxResults = Integer.MAX_VALUE;

    final List<EntityOrderByImpl<?>> orderBy = new ArrayList<>();

    private final Map<QueryParameter<?>, Object> queryParameters = new HashMap<>();

    RootFetchSetImpl(@NotNull QueryProviderImpl queryProvider, @NotNull Class<X> entityClass) {
        this.queryProvider = queryProvider;
        this.entityClass = entityClass;
    }

    @Override
    public Class<X> getEntityClass() {
        return entityClass;
    }

    @Override
    public RootFetchSet<?> getFetchRoot() {
        return this;
    }

    @Override
    public FetchSet<?> getFetchParent() {
        return null;
    }

    @Override
    public EntityJoin<?, X> getJoinedBy() {
        return null;
    }

    @Override
    public RootFetchSet<X> select(@NotNull EntityField<X, ?> selection) {
        return (RootFetchSet<X>) super.select(selection);
    }

    @Override
    public <Y> RootFetchSet<X> join(@NotNull EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
        return (RootFetchSet<X>) super.join(join, consumer);
    }

    @Override
    public RootFetchSet<X> filter(@NotNull EntityFilter<X> filter) {
        return (RootFetchSet<X>) super.filter(filter);
    }

    @Override
    public RootFetchSet<X> orderBy(@NotNull QueryField<X, ?> field, @NotNull SortDirection direction) {
        return (RootFetchSet<X>) super.orderBy(field, direction);
    }

    @Override
    public RootFetchSet<X> orderBy(@NotNull EntitySortKey<X> field, @NotNull SortDirection direction) {
        return (RootFetchSet<X>) super.orderBy(field, direction);
    }

    @Override
    public RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer) {
        return (RootFetchSet<X>) super.apply(consumer);
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public RootFetchSet<X> setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public RootFetchSet<X> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public List<EntityOrderBy<?>> getOrderBy() {
        return unmodifiableList(orderBy);
    }

    Map<QueryParameter<?>, Object> getQueryParameters() {
        return readOnlyCopy(queryParameters);
    }

    @Override
    public <T> RootFetchSet<X> setQueryParameter(@NotNull QueryParameter<T> parameter, T value) {
        requireNonNull(parameter, "parameter");
        queryParameters.put(parameter, value);
        return this;
    }

    @Override
    public QueryResultList<X> execute(@NotNull EntityRoot<X> entityRoot) {
        return new ExecutionContext(queryProvider).execute(entityRoot, this);
    }

    @Override
    public EntityResultList<X> entityQuery(@NotNull EntityRoot<X> entityRoot) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected RootFetchSetImpl<?> fetchRoot() {
        return this;
    }
}