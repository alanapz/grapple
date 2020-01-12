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
import org.grapple.utils.Utils;

final class EntityListQueryDataFetcher<X> implements DataFetcher<Map<String, Object>> {

    private final SchemaBuilderContext ctx;

    private final Class<X> entityClass;

    private final String queryName;

    EntityListQueryDataFetcher(SchemaBuilderContext ctx, Class<X> entityClass, String queryName) {
        this.ctx = requireNonNull(ctx, "ctx");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        this.queryName = requireNonNull(queryName, "queryName");
    }

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) {
        final SelectionSet selectionSet = SchemaUtils.walkFieldHierachy(environment.getField(), "results");
        if (selectionSet == null) {
            return buildResponse(0, emptyList());
        }
        final RootFetchSet<X> fetchSet = buildFetchSet(environment, selectionSet);
        final QueryResultList<X> results = ctx.executeEntityQuery(environment, entityClass, queryName, fetchSet, environment.getArguments());
        if (results == null || results.getTotalResults() == 0) {
            return buildResponse(0, emptyList());
        }
        if (results.getRowsRetrieved() == 0) {
            return buildResponse(results.getTotalResults(), emptyList());
        }
        final List<Map<String, Object>> response = new ArrayList<>();
        for (QueryResultRow<X> resultRow: results) {
            response.add(ctx.parseQueryResponse(environment, entityClass, selectionSet, resultRow));
        }
        return buildResponse(results.getTotalResults(), response);
    }

    private RootFetchSet<X> buildFetchSet(DataFetchingEnvironment environment, SelectionSet selectionSet) {
        final RootFetchSet<X> fetchSet = SchemaUtils.buildFetchSet(environment, queryName, entityClass);
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
        fetchSet.setMaxResults(coalesce((Integer) arguments.get("count"), Integer.MAX_VALUE));
        return fetchSet;
    }

    private Map<String, Object> buildResponse(int totalResults, List<Map<String, Object>> results) {
        final Map<String, Object> response = new HashMap<>();
        response.put("total", totalResults);
        response.put("results", results);
        return response;
    }
}