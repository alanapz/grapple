package org.grapple.schema.schema2;

import org.grapple.core.Chainable;
import org.grapple.query.EntityField;
import org.grapple.query.QueryField;
import org.grapple.schema.EntityDefinition;
import org.grapple.schema.EntitySchemaElement;

public interface EntityFieldConfiguration<X, T> extends EntitySchemaElement, Chainable<EntityFieldConfiguration<X, T>> {

    EntityDefinition<X> getEntity();

    

    EntityField<X, T> getField();

    QueryField<X, T> getQueryableField();

}
