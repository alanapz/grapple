package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import graphql.language.SelectionSet;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.grapple.query.QueryResultList;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.NonScalarQueryResultException;

final class EntityScalarQueryDataFetcher<X> implements DataFetcher<Map<String, Object>> {

    private final SchemaBuilderContext ctx;

    private final Class<X> entityClass;

    private final String queryName;

    EntityScalarQueryDataFetcher(SchemaBuilderContext ctx, Class<X> entityClass, String queryName) {
        this.ctx = requireNonNull(ctx, "ctx");
        this.entityClass = requireNonNull(entityClass, "entityClass");
        this.queryName = requireNonNull(queryName, "queryName");
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