package org.grapple.schema;

import graphql.schema.GraphQLInputType;
import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.Chainable;

public interface EntityCustomFilterDefinition<X, T> extends Chainable<EntityCustomFilterDefinition<X, T>> {

    EntityDefinition<X> getEntity();

    TypeLiteral<T> getFieldType();

    String getFieldName();

    void setFieldName(String fieldName);

    String getDescription();

    void setDescription(String description);

    GraphQLInputType getForcedGraphQLType();

    void setForcedGraphQLType(GraphQLInputType type);

    EntityCustomFilterResolver<X, T> getFilterResolver();

    void setFilterResolver(EntityCustomFilterResolver<X, T> filterResolver);

}
