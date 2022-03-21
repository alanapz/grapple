package org.grapple.schema.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.coalesce;
import static org.jooq.lambda.Seq.seq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graphql.language.SelectionSet;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntityQueryExecutionListener.QueryListenerContext;
import org.grapple.utils.Utils;

final class EntityListQueryDataFetcher<X> implements DataFetcher<Map<String, Object>> {

    private final SchemaBuilderContext ctx;

    private final Class<X> entityClass;

    private final String queryName;

    private final Object queryTag;

    EntityListQueryDataFetcher(SchemaBuilderContext ctx, Class<X> entityClass, String queryName, Object queryTag) {
        this.ctx = requireNonNull(ctx, "ctx");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        this.queryName = requireNonNull(queryName, "queryName");
        this.queryTag = queryTag;
    }

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) {
        final SelectionSet selectionSet = SchemaUtils.walkFieldHierachy(environment.getField(), "results");
        if (selectionSet == null) {
            return buildResponse(0, 0, 0, emptyList());
        }
        final RootFetchSet<X> fetchSet = buildFetchSet(environment, selectionSet);
        final QueryListenerContext queryListenerContext = ctx.getEntityQueryExecutionListeners().queryStarted(environment, fetchSet, queryName, queryTag);
        try {
            final Map<String, Object> response = executeQuery(environment, selectionSet, fetchSet);
            queryListenerContext.complete(response);
            return response;
        }
        catch (Exception e) {
            queryListenerContext.error(e);
            throw e;
        }
    }

    private Map<String, Object> executeQuery(DataFetchingEnvironment environment, SelectionSet selectionSet, RootFetchSet<X> fetchSet) {
        final QueryResultList<X> results = ctx.executeEntityQuery(environment, entityClass, queryName, fetchSet, environment.getArguments());
        requireNonNull(results, "results");
        if (results.getTotalResults() == 0 || results.getRowsRetrieved() == 0) {
            return buildResponse(fetchSet.getFirstResult(), fetchSet.getMaxResults(), results.getTotalResults(), emptyList());
        }
        final List<Map<String, Object>> response = new ArrayList<>();
        for (QueryResultRow<X> resultRow : results) {
            response.add(ctx.parseQueryResponse(environment, entityClass, selectionSet, resultRow));
        }
        return buildResponse(fetchSet.getFirstResult(), fetchSet.getMaxResults(), results.getTotalResults(), response);
    }

    private RootFetchSet<X> buildFetchSet(DataFetchingEnvironment environment, SelectionSet selectionSet) {
        final RootFetchSet<X> fetchSet = SchemaUtils.buildFetchSet(ctx, environment, queryName, entityClass);
        // First add all "selects"
        ctx.applyEntitySelection(environment, entityClass, fetchSet, selectionSet);
        final Map<String, Object> arguments = environment.getArguments();
        // Then where clauses
        if (arguments.containsKey("filter")) {
            final Map<String, Object> filter = Utils.reifyMap((Map<?, ?>) arguments.get("filter"));
            fetchSet.filter(ctx.generateEntityFilter(environment, entityClass, fetchSet, filter));
        }
        // Then order-by clauses
        if (arguments.containsKey("orderBy")) {
            final Collection<Map<String, Object>> orderBy = seq((Collection<?>) arguments.get("orderBy")).map(x -> (Map<?, ?>) x).map(Utils.reifyMap()).toList();
            for (Map<String, Object> args: orderBy) {
                ctx.applyEntityOrderBy(environment, entityClass, fetchSet, args);
            }
        }
        // Finally first/max results
        fetchSet.setFirstResult(coalesce((Integer) arguments.get("offset"), 0));
        fetchSet.setMaxResults(coalesce((Integer) arguments.get("count"), 1024));
        return fetchSet;
    }

    private Map<String, Object> buildResponse(int offset, int count, int totalResults, List<Map<String, Object>> results) {
        final Map<String, Object> response = new HashMap<>();
        response.put("offset", offset);
        response.put("count", count);
        response.put("total", totalResults);
        response.put("results", results);
        return response;
    }
}