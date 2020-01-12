package org.grapple.schema.impl;

import graphql.schema.GraphQLInputType;

interface GeneratedEntityFilterItem<X> {

    GraphQLInputType getInputType(GeneratedEntityFilter<X> source);

}
