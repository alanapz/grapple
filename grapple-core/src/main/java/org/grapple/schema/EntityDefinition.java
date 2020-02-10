package org.grapple.schema;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.grapple.core.Chainable;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.reflect.TypeLiteral;

public interface EntityDefinition<X> extends EntitySchemaElement, Chainable<EntityDefinition<X>> {

    Class<X> getEntityClass();

    <T> EntityFieldDefinition<X, T> addField(EntityField<X, T> field);

    Map<EntityField<X, ?>, ? extends EntityFieldDefinition<X, ?>> getFields();

    <Y> EntityJoinDefinition<X, Y> addJoin(EntityJoin<X, Y> join);

    Map<EntityJoin<X, ?>, ? extends EntityJoinDefinition<X, ?>> getJoins();

    <T> EntityFilterItemDefinition<X, T> addFilterItem(TypeLiteral<T> fieldType, Consumer<EntityFilterItemDefinition<X, T>> consumer);

    Set<? extends EntityFilterItemDefinition<X, ?>> getFilterItems();

    EntityQueryDefinition<X> addQuery(Consumer<EntityQueryDefinition<X>> consumer);

    Set<? extends EntityQueryDefinition<X>> getQueries();

}
