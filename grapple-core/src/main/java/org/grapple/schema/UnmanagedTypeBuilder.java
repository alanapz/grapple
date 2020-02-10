package org.grapple.schema;

import graphql.schema.GraphQLObjectType;

@FunctionalInterface
public interface UnmanagedTypeBuilder {

    GraphQLObjectType build(SchemaBuilderContext ctx);

}
