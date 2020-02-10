package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.query.EntityJoin;

public interface EntityJoinDefinition<X, Y> extends EntitySchemaElement, Chainable<EntityJoinDefinition<X, Y>> {

    EntityDefinition<X> getEntity();

    EntityJoin<X, Y> getJoin();

    EntityDefinition<Y> getJoinedEntity();

}
