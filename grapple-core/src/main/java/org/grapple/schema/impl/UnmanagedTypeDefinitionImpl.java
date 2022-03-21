package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.GraphQLObjectType;
import org.grapple.core.ElementVisibility;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.UnmanagedTypeBuilder;
import org.grapple.schema.UnmanagedTypeDefinition;
import org.grapple.utils.NoDuplicatesMap;

final class UnmanagedTypeDefinitionImpl<T> implements UnmanagedTypeDefinition<T> {

    private final TypeLiteral<T> type;

    private ElementVisibility visibility;

    private UnmanagedTypeBuilder typeBuilder;

    private final Map<String, DataFetcher<?>> dataFetchers = new NoDuplicatesMap<>();

    UnmanagedTypeDefinitionImpl(TypeLiteral<T> type) {
        this.type = requireNonNull(type, "type");
    }

    @Override
    public TypeLiteral<T> getType() {
        return type;
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
    public UnmanagedTypeBuilder getTypeBuilder() {
        return typeBuilder;
    }

    @Override
    public void setTypeBuilder(UnmanagedTypeBuilder typeBuilder) {
        requireNonNull(typeBuilder, "typeBuilder");
        this.typeBuilder = typeBuilder;
    }

    @Override
    public Map<String, DataFetcher<?>> getDataFetchers() {
        return readOnlyCopy(dataFetchers);
    }

    @Override
    public void addDataFetcher(String fieldName, DataFetcher<?> dataFetcher) {
        requireNonNull(fieldName, "fieldName");
        requireNonNull(dataFetcher, "dataFetcher");
        dataFetchers.put(fieldName, dataFetcher);
    }

    @Override
    public void validate() {
        if (typeBuilder == null) {
            throw new IllegalArgumentException("typeBuilder not configured");
        }
    }

    void build(SchemaBuilderContext ctx) {
        validate();
        if (!ctx.isSchemaElementVisible(visibility)) {
            return;
        }
        final GraphQLObjectType graphQLType = typeBuilder.build(ctx);
        if (graphQLType == null) {
            return;
        }
        ctx.addUnmanagedType(type.getType(), graphQLType);
        for (Map.Entry<String, DataFetcher<?>> dataFetcher: dataFetchers.entrySet()) {
            ctx.addUnmanagedDataFetcher(graphQLType.getName(), dataFetcher.getKey(), dataFetcher.getValue());
        }
    }
}
