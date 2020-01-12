package org.grapple.schema;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.Chainable;

public interface EntityDefinition<X> extends Chainable<EntityDefinition<X>> {

    Class<X> getEntityClass();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    <T> EntityFieldDefinition<X, T> addField(EntityField<X, T> field);

    Map<EntityField<X, ?>, ? extends EntityFieldDefinition<X, ?>> getFields();

    <Y> EntityJoinDefinition<X, Y> addJoin(EntityJoin<X, Y> join);

    Map<EntityJoin<X, ?>, ? extends EntityJoinDefinition<X, ?>> getJoins();

    <T> EntityCustomFilterDefinition<X, T> addCustomFilter(TypeLiteral<T> fieldType, Consumer<EntityCustomFilterDefinition<X, T>> consumer);

    Set<? extends EntityCustomFilterDefinition<X, ?>> getCustomFilters();

    EntityQueryDefinition<X> addQuery(Consumer<EntityQueryDefinition<X>> consumer);

    Set<? extends EntityQueryDefinition<X>> getQueries();

}
