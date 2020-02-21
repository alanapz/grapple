package org.grapple.query.impl;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.grapple.query.EntityContext;
import org.grapple.query.EntityField;
import org.grapple.query.EntityFilter;
import org.grapple.query.EntityJoin;
import org.grapple.query.EntityRoot;
import org.grapple.query.FetchSet;
import org.grapple.query.NullNotAllowedException;
import org.grapple.query.QueryBuilder;
import org.grapple.utils.LazyValue;

final class ExecutionContext {

    private final EntityManager entityManager;

    private final QueryBuilder queryBuilder;

    private final Map<FetchSet<?>, EntityContext<?>> fetchSets = new HashMap<>();

    private final List<QueryRowCallback> fieldCallbacks = new ArrayList<>();

    ExecutionContext(EntityManager entityManager) {
        this.entityManager = requireNonNull(entityManager, "entityManager");
        this.queryBuilder = new QueryBuilderImpl(entityManager.getCriteriaBuilder());
    }

    @SuppressWarnings("unchecked")
    <X> EntityContext<X> getEntityContext(FetchSet<X> fetchSet) {
        return (EntityContext<X>) Objects.requireNonNull(fetchSets.get(fetchSet), QueryImplUtils.resolveFullName(fetchSet));
    }

    <X> QueryResultListImpl<X> execute(EntityRoot<X> entityRoot, RootFetchSetImpl<X> fetches) {
        requireNonNull(entityRoot, "entityRoot");
        requireNonNull(fetches, "fetches");

        final int totalResults = countTupleQueryResults(entityRoot, fetches);

        if (totalResults == 0) {
            return new QueryResultListImpl<>(fetches, 0, emptyList());
        }

        if (QueryImplUtils.isEmptyFetchSet(fetches)) {
            return new QueryResultListImpl<>(fetches, totalResults, emptyList());
        }

        final CriteriaQuery<Tuple> criteriaQuery = queryBuilder.createTupleQuery();
        criteriaQuery.distinct(true);

        final QueryWrapper queryWrapper = new QueryWrapper(criteriaQuery, queryBuilder);

        final Root<X> queryRoot = criteriaQuery.from(entityRoot.getEntityClass());
        final EntityContext<X> rootEntityContext = buildEntityContext(queryWrapper, fetches, queryRoot, true);
        applyFilter(queryWrapper, rootEntityContext, entityRoot.getFilter());

        for (EntityOrderByImpl<?> orderBy: fetches.orderBy) {
            queryWrapper.orderBy(orderBy.build(this, queryBuilder));
        }

        final TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(fetches.getFirstResult());
        typedQuery.setMaxResults(fetches.getMaxResults());

        final List<Tuple> rows = typedQuery.getResultList();

        final List<TabularResultRowImpl> resultRows = new ArrayList<>(rows.size());

        for (Tuple row: rows) {
            final TabularResultRowImpl resultItem = new TabularResultRowImpl();
            for (QueryRowCallback queryRowCallback: fieldCallbacks) {
                queryRowCallback.execute(row, resultItem);
            }
            resultRows.add(resultItem);
        }

        return new QueryResultListImpl<>(fetches, totalResults, resultRows);
    }

    private <X> int countTupleQueryResults(EntityRoot<X> entityRoot, RootFetchSetImpl<X> fetches) {
        final CriteriaQuery<Integer> criteriaQuery = queryBuilder.createQuery(Integer.class);
        criteriaQuery.distinct(true);

        final QueryWrapper queryWrapper = new QueryWrapper(criteriaQuery, queryBuilder);

        final Root<X> queryRoot = criteriaQuery.from(entityRoot.getEntityClass());
        final EntityContext<X> rootEntityContext = buildEntityContext(queryWrapper, fetches, queryRoot, false);
        applyFilter(queryWrapper, rootEntityContext, entityRoot.getFilter());

        // Clear all selections, replace with a simple count
        criteriaQuery.select(queryBuilder.toInteger(queryBuilder.count(queryRoot)));

        final TypedQuery<Integer> typedQuery = entityManager.createQuery(criteriaQuery);

        return typedQuery.getSingleResult();
    }

    private <X> EntityContext<X> buildEntityContext(QueryWrapper query, RootFetchSetImpl<X> rootFetchSet, Root<X> entity, boolean fetchSelections) {
        return processEntityContext(query, rootFetchSet, new EntityContextImpl<>(rootFetchSet, query, queryBuilder, LazyValue.fixed(entity)), fetchSelections);
    }

    // Q1 and Q2 are virtual type parameters used for type-safety whilst looping over fields
    private <X, Q1, Q2> EntityContext<X> processEntityContext(QueryWrapper query, FetchSet<X> fetchSet, AbstractEntityContextImpl<X> entityContext, boolean fetchSelections) {
        // Only add selections if we are in fetch mode (not count query for example)
        if (fetchSelections) {
            fetchSets.put(fetchSet, entityContext);

            // https://github.com/alanapz/grapple/issues/1
            // If we have at least one selection, then force the selection of primary key IS NOT NULL
            // This is so we can detect null fields versus null joins
            if (fetchSet.getJoinedBy() != null) {
                fieldCallbacks.add(new EntityExistsResultCallback<>(fetchSet, entityContext));
            }

            for (EntityField<X, ?> rawField: fetchSet.getSelections()) {
                final @SuppressWarnings("unchecked") EntityField<X, Q1> field = (EntityField<X, Q1>) rawField;
                final Function<Tuple, Q1> resultHandler = field.prepare(entityContext, queryBuilder);
                fieldCallbacks.add(new ExecutionContext.SelectionResultCallback<>(fetchSet, field, resultHandler));
            }
        }

        for (EntityFilter<X> filter: fetchSet.getFilters()) {
            applyFilter(query, entityContext, filter);
        }

        for (Map.Entry<EntityJoin<X, ?>, FetchSet<?>> joinEntry: fetchSet.getJoins().entrySet()) {
            final @SuppressWarnings("unchecked") EntityJoin<X, Q2> entityJoin = (EntityJoin<X, Q2>) joinEntry.getKey();
            final @SuppressWarnings("unchecked") FetchSet<Q2> joinedFetchSet = (FetchSet<Q2>) joinEntry.getValue();
            if (!QueryImplUtils.isEmptyFetchSet(joinedFetchSet)) { // Only join with non-empty
                final JoinedEntityContextImpl<Q2> childEntityContext = entityContext.join(entityJoin);
                processEntityContext(query, joinedFetchSet, childEntityContext, fetchSelections);
            }
        }
        return entityContext;
    }

    private <X> void applyFilter(QueryWrapper query, EntityContext<X> entityContext, EntityFilter<X> filter) {
        if (filter.isAlwaysTrue()) {
            return;
        }
        if (filter.isAlwaysFalse()) {
            query.where(queryBuilder.alwaysFalse());
            return;
        }
        query.where(filter.apply(entityContext, queryBuilder));
    }

    private interface QueryRowCallback {

        void execute(Tuple row, TabularResultRowImpl resultItem);
    }

    private final class EntityExistsResultCallback<X> implements QueryRowCallback {

        private final FetchSet<X> fetchSet;

        private final EntityJoin<?, X> joinedBy;

        private final Predicate entityExistsPath;

        private EntityExistsResultCallback(FetchSet<X> fetchSet, AbstractEntityContextImpl<X> entityCtx) {
            this.fetchSet = requireNonNull(fetchSet, "fetchSet");
            this.joinedBy = requireNonNull(fetchSet.getJoinedBy(), "joinedBy");
            this.entityExistsPath = entityCtx.addSelection(queryBuilder.isNotNull(entityCtx.getEntity()));
        }

        @Override
        public void execute(Tuple row, TabularResultRowImpl resultItem) {
            final boolean entityExists =  requireNonNull(row.get(entityExistsPath), "entityExistsPath");
            if (!entityExists && !joinedBy.getResultType().isNullAllowed() && resultItem.isExists(fetchSet.getFetchParent())) {
                throw new NullNotAllowedException(format("Null result for non-null join: %s", QueryImplUtils.resolveFullName(fetchSet)));
            }
            if (entityExists) {
                resultItem.setEntityExists(fetchSet);
            }
        }
    }

    private static final class SelectionResultCallback<X, T> implements QueryRowCallback {

        private final FetchSet<X> fetchSet;

        private final EntityField<X, T> field;

        private final Function<Tuple, T> resultHandler;

        private SelectionResultCallback(FetchSet<X> fetchSet, EntityField<X, T> field, Function<Tuple, T> resultHandler) {
            this.fetchSet = requireNonNull(fetchSet, "fetchSet");
            this.field = requireNonNull(field, "field");
            this.resultHandler = requireNonNull(resultHandler, "resultHandler");
        }

        @Override
        public void execute(Tuple row, TabularResultRowImpl resultItem) {
            final T result = resultHandler.apply(row);
            // Null-checking: Throw error if result is null, yet field type doesn't allow nulls, and we are not null because our parent wasn't fetched
            if (result == null && !field.getResultType().isNullAllowed() && resultItem.isExists(fetchSet)) {
                throw new NullNotAllowedException(format("Null result for non-null field: %s", QueryImplUtils.resolveFullName(fetchSet, field.getName())));
            }
            resultItem.setValue(fetchSet, field, result);
        }
    }
}
