package org.grapple.schema;

import graphql.schema.GraphQLEnumType;

public interface EnumTypeBuilder {

    GraphQLEnumType buildEnumType(Class<? extends Enum<?>> enumClass);

}
