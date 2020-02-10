package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;
import java.util.function.Function;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import org.grapple.core.ElementVisibility;
import org.grapple.schema.UnmanagedQueryBuilder;
import org.grapple.schema.UnmanagedQueryDefinition;
import org.grapple.utils.Utils;

public class UnmanagedQueryDefinitionImpl implements UnmanagedQueryDefinition {

    private final String queryAlias;

    private ElementVisibility visibility;

    private UnmanagedQueryBuilder queryBuilder;

    private DataFetcher<?> dataFetcher;

    UnmanagedQueryDefinitionImpl(String queryAlias) {
        this.queryAlias = requireNonNull(queryAlias, "queryAlias");
    }

    @Override
    public String getQueryAlias() {
        return queryAlias;
    }

    @Override
    public ElementVisibility getVisibility() {
        return visibility;
    }

    @Override
    public void setVisibility(ElementVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public UnmanagedQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    @Override
    public void setQueryBuilder(UnmanagedQueryBuilder queryBuilder) {
        requireNonNull(queryBuilder, "queryBuilder");
        this.queryBuilder = queryBuilder;
    }

    @Override
    public DataFetcher<?> getDataFetcher() {
        return dataFetcher;
    }

    @Override
    public void setDataFetcher(DataFetcher<?> dataFetcher) {
        requireNonNull(dataFetcher, "dataFetcher");
        this.dataFetcher = dataFetcher;
    }

    @Override
    public void validate() {
        if (queryBuilder == null) {
            throw new IllegalArgumentException("queryBuilder not configured");
        }
        if (dataFetcher == null) {
            throw new IllegalArgumentException("dataFetcher not configured");
        }
    }

    void build(SchemaBuilderContext ctx) {
        validate();
        if (!ctx.isSchemaElementVisible(visibility)) {
            return;
        }
        final GraphQLFieldDefinition graphQLFieldDefinition = queryBuilder.build(ctx);
        if (graphQLFieldDefinition == null) {
            return;
        }
        ctx.addRootQueryField(graphQLFieldDefinition);
        ctx.addUnmanagedDataFetcher(ctx.getRootQueryTypeName(), graphQLFieldDefinition.getName(), dataFetcher);
    }

    @Override
    public UnmanagedQueryDefinition apply(Consumer<UnmanagedQueryDefinition> consumer) {
        return Utils.apply(this, consumer);
    }

    @Override
    public <Z> Z invoke(Function<UnmanagedQueryDefinition, Z> function) {
        return requireNonNull(function, "function").apply(this);
    }
}
