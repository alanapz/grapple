package org.grapple.schema;

import java.lang.reflect.Type;
import graphql.schema.GraphQLType;

public interface SchemaBuilderContext {

    GraphQLType buildTypeFor(Type type);

}
