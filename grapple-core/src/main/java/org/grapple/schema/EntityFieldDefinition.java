package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.query.EntityField;
import org.grapple.query.QueryField;

public interface EntityFieldDefinition<X, T> extends EntitySchemaElement, Chainable<EntityFieldDefinition<X, T>> {

    EntityDefinition<X> getEntity();

    EntityField<X, T> getField();

    QueryField<X, T> getQueryableField();

}
