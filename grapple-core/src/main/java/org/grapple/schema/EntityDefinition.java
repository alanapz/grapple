package org.grapple.schema;

import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.utils.Chainable;

public interface EntityDefinition<X> extends Chainable<EntityDefinition<X>> {

    String getName();

    EntityDefinition<X> setName(String name);

    EntityDefinition<X> addField(EntityField<X, ?> field);

    EntityDefinition<X> addJoin(EntityJoin<X, ?> join);

    EntityDefinition<X> importFrom(Class<?> definitionClass);
}
