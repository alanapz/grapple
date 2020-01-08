package org.grapple.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.grapple.utils.LazyValue;

import static java.util.Objects.requireNonNull;
import static org.grapple.query.Utils.cast;

final class ExecutionContext {

    private final EntityManager entityManager;

    private final QueryBuilder builder;

    private final Map<FetchSet<?>, EntityContext<?>> fetchSets = new HashMap<>();

    private final List<FieldCallback<?, ?>> fieldCallbacks = new ArrayList<>();

    ExecutionContext(EntityManager entityManager) {
        this.entityManager = requireNonNull(entityManager, "entityManager");
        this.builder = new QueryBuilder(entityManager.getCriteriaBuilder());
    }

    <X> EntityContext<X> getEntityContext(FetchSet<X> fetchSet) {
        return cast(requireNonNull(fetchSets.get(fetchSet), Utils.getFullName(fetchSet, null)));
    }

    <X> QueryResultList execute(EntityRoot<X> entityRoot, RootFetchSet<X> fetches) {
        requireNonNull(fetches, "fetches");

        final int totalResults = countTupleQueryResults(entityRoot, fetches);

        if (totalResults == 0) {
            return QueryResultList.empty(0);
        }

        if (Utils.isEmptyFetchSet(fetches)) {
            return QueryResultList.empty(totalResults);
        }

        final CriteriaQuery<Tuple> criteriaQuery = builder.createTupleQuery();
        criteriaQuery.distinct(true);

        final QueryWrapper queryWrapper = new QueryWrapper(criteriaQuery, builder);

        final Root<X> queryRoot = criteriaQuery.from(entityRoot.getEntityClass());
        final EntityContext<X> rootEntityContext = buildEntityContext(queryWrapper, fetches, queryRoot, true);
        applyFilter(queryWrapper, rootEntityContext, entityRoot.getFilter());

        for (EntityOrderBy<?> orderBy: fetches.getOrderBy()) {
            queryWrapper.orderBy(orderBy.build(this, builder));
        }

        final TypedQuery<Tuple> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult(fetches.getFirstResult());
        typedQuery.setMaxResults(fetches.getMaxResults());

        final List<Tuple> rows = typedQuery.getResultList();

        final QueryResultList resultList = QueryResultList.results(totalResults);

        for (Tuple row: rows) {
            final QueryResultItem resultItem = new QueryResultItem();
            for (FieldCallback<?, ?> fieldCallback: fieldCallbacks) {
                fieldCallback.execute(row, resultItem);
            }
            resultList.getResults().add(resultItem);
        }

        return resultList;
    }

    private <X> int countTupleQueryResults(EntityRoot<X> entityRoot, FetchSet<X> fetches) {
        final CriteriaQuery<Integer> criteriaQuery = builder.createQuery(Integer.class);
        criteriaQuery.distinct(true);

        final QueryWrapper queryWrapper = new QueryWrapper(criteriaQuery, builder);

        final Root<X> queryRoot = criteriaQuery.from(entityRoot.getEntityClass());
        final EntityContext<X> rootEntityContext = buildEntityContext(queryWrapper, fetches, queryRoot, false);
        applyFilter(queryWrapper, rootEntityContext, entityRoot.getFilter());

        // Clear all selections, replace with a simple count
        criteriaQuery.select(builder.toInteger(builder.count(queryRoot)));

        final TypedQuery<Integer> typedQuery = entityManager.createQuery(criteriaQuery);

        return typedQuery.getSingleResult();
    }

    private <X> EntityContext<X> buildEntityContext(QueryWrapper query, FetchSet<X> fetchSet, Root<X> entity, boolean fetchSelections) {
        return processEntityContext(query, fetchSet, new EntityContextImpl<>(query, builder, LazyValue.fixed(entity)), fetchSelections);
    }

    private <X> EntityContext<X> processEntityContext(QueryWrapper query, FetchSet<X> fetchSet, EntityContextImpl<X> entityContext, boolean fetchSelections) {
        // Only add selections if we are in fetch mode (not count query for example)
        if (fetchSelections) {
            fetchSets.put(fetchSet, entityContext);
            for (EntityField<X, ?> field: fetchSet.getSelections()) {
                final Function<Tuple, ?> resultHandler = field.prepare(entityContext, builder);
                fieldCallbacks.add(new FieldCallback<>(fetchSet, cast(field), cast(resultHandler))); // Can't do much better...
            }
        }
        for (EntityFilter<X> filter: fetchSet.getFilters()) {
            applyFilter(query, entityContext, filter);
        }
        for (Map.Entry<EntityJoin<X, ?>, FetchSet<?>> joinEntry: fetchSet.getJoins().entrySet()) {
            final FetchSet<?> joinedFetchSet = joinEntry.getValue();
            if (!Utils.isEmptyFetchSet(joinedFetchSet)) { // Only join with non-empty
                final EntityContext<?> childEntityContext = entityContext.join(joinEntry.getKey());
                processEntityContext(query, cast(joinedFetchSet), cast(childEntityContext), fetchSelections);
            }
        }
        return entityContext;
    }


    private <X> void applyFilter(QueryWrapper query, EntityContext<X> entityContext, EntityFilter<X> filter) {
        if (filter.isAlwaysTrue()) {
            return;
        }
        if (filter.isAlwaysFalse()) {
            query.where(builder.alwaysFalse());
            return;
        }
        query.where(filter.apply(entityContext, builder));
    }

    private static final class FieldCallback<X, T> {

        private final FetchSet<X> fetchSet;

        private final EntityField<X, T> field;

        private final Function<Tuple, T> resultHandler;

        private FieldCallback(FetchSet<X> fetchSet, EntityField<X, T> field, Function<Tuple, T> resultHandler) {
            this.fetchSet = requireNonNull(fetchSet, "fetchSet");
            this.field = requireNonNull(field, "field");
            this.resultHandler = requireNonNull(resultHandler, "resultHandler");
        }

        private void execute(Tuple row, QueryResultItem resultItem) {
            final T result = resultHandler.apply(row);
            resultItem.set(fetchSet, field, result);
        }
    }
}
