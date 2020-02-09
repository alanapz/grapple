package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.query.EntityField;
import org.grapple.query.QueryField;

public interface EntityFieldDefinition<X, T> extends Chainable<EntityFieldDefinition<X, T>> {

    EntityDefinition<X> getEntity();

    EntityField<X, T> getField();

    QueryField<X, T> getQueryableField();

    String getFieldName();

    void setFieldName(String fieldName);

    String getDescription();

    void setDescription(String description);

    String getDeprecationReason();

    void setDeprecationReason(String deprecationReason);
}
