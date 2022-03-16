package org.grapple.entity;

import java.util.Set;
import java.util.function.Consumer;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntityQueryDefinitionParameter;
import org.grapple.schema.EntityQueryResolver;
import org.grapple.schema.EntityQueryType;

public class EntityRootDefinitionBuilder {

        EntityDefinition<X> getEntity();

        EntityQueryType getQueryType();

        void setQueryType(EntityQueryType queryType);

        Set<? extends EntityQueryDefinitionParameter<?>> getParameters();

        <T> EntityQueryDefinitionParameter<T> addParameter(TypeLiteral<T> type, Consumer<EntityQueryDefinitionParameter<T>> consumer);

        EntityQueryResolver<X> getQueryResolver();

        void setQueryResolver(EntityQueryResolver<X> queryResolver);

}
