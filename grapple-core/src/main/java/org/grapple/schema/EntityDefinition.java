package org.grapple.schema;

import java.util.Map;
import java.util.function.Consumer;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.utils.Chainable;

public interface EntityDefinition<X> extends Chainable<EntityDefinition<X>> {

    EntityDefinition<X> setName(String name);

    EntityDefinition<X> setDescription(String description);

    EntityDefinition<X> addField(EntityField<X, ?> field);

    <T> EntityDefinition<X> addField(EntityField<X, T> field, Consumer<EntityFieldDefinition> consumer);

    Map<EntityField<X, ?>, EntityFieldDefinition> getFields();

    EntityDefinition<X> addJoin(EntityJoin<X, ?> join);

    EntityDefinition<X> addJoin(EntityJoin<X, ?> join, Consumer<EntityJoinDefinition> consumer);

    Map<EntityJoin<X, ?>, EntityJoinDefinition> getJoins();

    EntityDefinition<X> importFrom(Class<?> definitionClass);

}
