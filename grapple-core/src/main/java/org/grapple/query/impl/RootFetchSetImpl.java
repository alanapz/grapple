package org.grapple.query.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.persistence.EntityManager;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityOrderBy;
import org.grapple.query.EntityRoot;
import org.grapple.query.FetchSet;
import org.grapple.query.QueryField;
import org.grapple.query.QueryParameter;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.query.SortDirection;
import org.grapple.utils.EntitySortKey;

final class RootFetchSetImpl<X> extends AbstractFetchSetImpl<X> implements RootFetchSet<X> {

    private final Class<X> entityClass;

    private int firstResult = 0;

    private int maxResults = Integer.MAX_VALUE;

    final List<EntityOrderByImpl<?>> orderBy = new ArrayList<>();

    private final Map<QueryParameter<?>, Object> queryParameters = new HashMap<>();

    RootFetchSetImpl(Class<X> entityClass) {
        this.entityClass = requireNonNull(entityClass, "entityClass");
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
    public RootFetchSet<X> select(EntityField<X, ?> selection) {
        return (RootFetchSet<X>) super.select(selection);
    }

    @Override
    public <Y> RootFetchSet<X> join(EntityJoin<X, Y> join, Consumer<FetchSet<Y>> consumer) {
        return (RootFetchSet<X>) super.join(join, consumer);
    }

    @Override
    public RootFetchSet<X> filter(EntityFilter<X> filter) {
        return (RootFetchSet<X>) super.filter(filter);
    }

    @Override
    public RootFetchSet<X> orderBy(QueryField<X, ?> field, SortDirection direction) {
        return (RootFetchSet<X>) super.orderBy(field, direction);
    }

    @Override
    public RootFetchSet<X> orderBy(EntitySortKey<X> field, SortDirection direction) {
        return (RootFetchSet<X>) super.orderBy(field, direction);
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
        return unmodifiableMap(queryParameters);
    }

    @Override
    public <T> RootFetchSet<X> setQueryParameter(QueryParameter<T> parameter, T value) {
        requireNonNull(parameter, "parameter");
        queryParameters.put(parameter, value);
        return this;
    }

    @Override
    public QueryResultList<X> execute(EntityManager entityManager, EntityRoot<X> entityRoot) {
        requireNonNull(entityManager, "entityManager");
        requireNonNull(entityRoot, "entityRoot");
        return new ExecutionContext(entityManager).execute(entityRoot, this);
    }

    @Override
    public RootFetchSet<X> apply(Consumer<FetchSet<X>> consumer) {
        return (RootFetchSet<X>) super.apply(consumer);
    }

    @Override
    protected RootFetchSetImpl<?> fetchRoot() {
        return this;
    }
}