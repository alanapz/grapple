package org.grapple.schema;

import graphql.schema.GraphQLEnumType;

public interface EnumTypeBuilder {

    <E extends Enum<E>> GraphQLEnumType buildEnumType(Class<E> enumClass);

}
