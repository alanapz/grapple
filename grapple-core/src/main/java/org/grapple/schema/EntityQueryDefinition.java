package org.grapple.schema;

import java.util.Set;
import java.util.function.Consumer;
import org.grapple.core.Chainable;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;

public interface EntityQueryDefinition<X> extends EntitySchemaElement, Chainable<EntityQueryDefinition<X>>, Validatable {

    EntityDefinition<X> getEntity();

    EntityQueryType getQueryType();

    void setQueryType(EntityQueryType queryType);

    Set<? extends EntityQueryDefinitionParameter<?>> getParameters();

    <T> EntityQueryDefinitionParameter<T> addParameter(TypeLiteral<T> type, Consumer<EntityQueryDefinitionParameter<T>> consumer);

    EntityQueryResolver<X> getQueryResolver();

    void setQueryResolver(EntityQueryResolver<X> queryResolver);

}
