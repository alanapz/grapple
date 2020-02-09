package org.grapple.schema;

import org.grapple.query.EntityJoin;
import org.grapple.utils.Chainable;

public interface EntityJoinDefinition<X, Y> extends Chainable<EntityJoinDefinition<X, Y>> {

    EntityDefinition<X> getEntity();

    EntityJoin<X, Y> getJoin();

    EntityDefinition<Y> getJoinedEntity();

    String getFieldName();

    void setFieldName(String fieldName);

    String getDescription();

    void setDescription(String description);

    String getDeprecationReason();

    void setDeprecationReason(String deprecationReason);

}
