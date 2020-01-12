package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;

import graphql.schema.GraphQLInputType;
import org.grapple.schema.impl.RuntimeWiring.FieldFilterCallback;

public final class SimpleFieldFilterItem<T> {

    public final String name;

    public final GraphQLInputType gqlType;

    public final FieldFilterCallback<T> resolver;

    private SimpleFieldFilterItem(String name, GraphQLInputType gqlType, FieldFilterCallback<T> resolver) {
        this.name = requireNonNull(name, "name");
        this.gqlType = requireNonNull(gqlType, "gqlType");
        this.resolver = requireNonNull(resolver, "resolver");
    }

    static <T> SimpleFieldFilterItem<T> simpleFieldFilterItem(String name, GraphQLInputType gqlType, FieldFilterCallback<T> resolver) {
        return new SimpleFieldFilterItem<>(name, gqlType, resolver);
    }
}

