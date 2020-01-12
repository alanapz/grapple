package org.grapple.schema;

import org.grapple.query.EntityField;
import org.grapple.query.QueryField;
import org.grapple.utils.Chainable;

public interface EntityFieldDefinition<X, T> extends Chainable<EntityFieldDefinition<X, T>> {

    EntityDefinition<X> getEntity();

    EntityField<X, T> getField();

    QueryField<X, T> getQueryableField();

    String getFieldName();

    void setFieldName(String fieldName);

    void setDescription(String description);

    void setIsDeprecated(boolean deprecated);

    void setDeprecationReason(String deprecationReason);
}
