package org.grapple.schema;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import graphql.schema.GraphQLFieldDefinition;
import org.grapple.utils.Chainable;

public interface EntityFieldDefinition<X> extends Chainable<EntityFieldDefinition<X>> {

    String getName();

    EntityFieldDefinition<X> setName(String name);

    EntityFieldDefinition<X> setDescription(String description);

    EntityFieldDefinition<X> configure(UnaryOperator<GraphQLFieldDefinition.Builder> builder);
}
