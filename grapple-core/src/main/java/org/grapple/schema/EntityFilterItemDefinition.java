package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;

public interface EntityFilterItemDefinition<X, T> extends EntitySchemaElement, Chainable<EntityFilterItemDefinition<X, T>>, Validatable {

    EntityDefinition<X> getEntity();

    TypeLiteral<T> getFieldType();

    EntityFilterItemResolver<X, T> getFilterResolver();

    void setFilterResolver(EntityFilterItemResolver<X, T> filterResolver);

}
