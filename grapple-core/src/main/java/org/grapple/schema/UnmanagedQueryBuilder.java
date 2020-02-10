package org.grapple.schema;

import graphql.schema.GraphQLFieldDefinition;

@FunctionalInterface
public interface UnmanagedQueryBuilder {

    GraphQLFieldDefinition build(SchemaBuilderContext ctx);

}
