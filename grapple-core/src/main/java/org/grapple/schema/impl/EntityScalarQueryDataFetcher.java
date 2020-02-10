package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import graphql.language.SelectionSet;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntityQueryExecutionListener.QueryListenerContext;
import org.grapple.schema.NonScalarQueryResultException;

final class EntityScalarQueryDataFetcher<X> implements DataFetcher<Map<String, Object>> {

    private final SchemaBuilderContext ctx;

    private final Class<X> entityClass;

    private final String queryName;

    private final Object queryTag;

    EntityScalarQueryDataFetcher(SchemaBuilderContext ctx, Class<X> entityClass, String queryName, Object queryTag) {
        this.ctx = requireNonNull(ctx, "ctx");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        this.queryName = requireNonNull(queryName, "queryName");
        this.queryTag = queryTag;
    }

    @Override
    public Map<String, Object> get(DataFetchingEnvironment environment) {
        final SelectionSet selectionSet = SchemaUtils.walkFieldHierachy(environment.getField());
        if (selectionSet == null) {
            return null;
        }
        final RootFetchSet<X> fetchSet = SchemaUtils.buildFetchSet(environment, queryName, entityClass);
        ctx.applyEntitySelection(environment, entityClass, fetchSet, selectionSet);
        fetchSet.setMaxResults(2); // So we can detect if we have non-unique results
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
        if (results == null || results.getRowsRetrieved() == 0) {
            return null;
        }
        if (results.getRowsRetrieved() != 1) {
            throw new NonScalarQueryResultException(String.format("Scalar query: %s returned multiple rows", queryName));
        }
        return ctx.parseQueryResponse(environment, entityClass, selectionSet, results.iterator().next());
    }
}