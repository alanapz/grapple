package org.grapple.schema;

import java.util.function.Function;
import graphql.schema.GraphQLInputType;
import org.grapple.query.EntityFilter;

import static java.util.Objects.requireNonNull;

public final class EntityFilterDefinition<X> implements Function<Object, EntityFilter<X>> {

    private final String name;

    private final GraphQLInputType type;

    private final Function<Object, EntityFilter<X>> filterGenerator;

    private EntityFilterDefinition(String name, GraphQLInputType type, Function<Object, EntityFilter<X>> filterGenerator) {
        this.name = requireNonNull(name, "name");
        this.type = requireNonNull(type, "type");
        this.filterGenerator = requireNonNull(filterGenerator, "filterGenerator");
    }

    public String getName() {
        return name;
    }

    public GraphQLInputType getType() {
        return type;
    }

    @Override
    public EntityFilter<X> apply(Object args) {
        return filterGenerator.apply(args);
    }

    static <X> EntityFilterDefinition<X> of(String name, GraphQLInputType type, Function<Object, EntityFilter<X>> filterGenerator) {
        return new EntityFilterDefinition<>(name, type, filterGenerator);
    }
}
