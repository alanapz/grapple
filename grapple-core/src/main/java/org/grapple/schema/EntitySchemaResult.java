package org.grapple.schema;

import java.util.Set;
import graphql.schema.GraphQLSchema;

public interface EntitySchemaResult {

    GraphQLSchema getSchema();

    GraphQLSchema getSchemaWithVisibility(Set<String> rolesHeld);

}
