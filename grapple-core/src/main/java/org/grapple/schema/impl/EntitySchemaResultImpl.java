package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import graphql.schema.GraphQLSchema;
import graphql.schema.visibility.GraphqlFieldVisibility;
import org.grapple.schema.EntitySchemaResult;

final class EntitySchemaResultImpl implements EntitySchemaResult {

    private final GraphQLSchema schema;

    private final SchemaBuilderElementVisibility schemaBuilderElementVisibility;

    EntitySchemaResultImpl(GraphQLSchema schema, SchemaBuilderElementVisibility schemaBuilderElementVisibility) {
        this.schema = requireNonNull(schema, "schema");
        this.schemaBuilderElementVisibility = requireNonNull(schemaBuilderElementVisibility, "schemaBuilderElementVisibility");
    }

    @Override
    public GraphQLSchema getSchema() {
        return schema;
    }

    @Override
    public GraphQLSchema getSchemaWithVisibility(Set<String> rolesHeld) {
        requireNonNull(rolesHeld, "rolesHeld");
        final GraphqlFieldVisibility graphqlFieldVisibility = schemaBuilderElementVisibility.compileVisibility(rolesHeld);
        return GraphQLSchema.newSchema(schema)
                .codeRegistry(schema.getCodeRegistry().transform(codeRegistry -> codeRegistry.fieldVisibility(graphqlFieldVisibility)))
                .build();
    }
}
