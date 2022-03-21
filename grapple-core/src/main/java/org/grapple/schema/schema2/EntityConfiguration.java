package org.grapple.schema.schema2;

import java.util.function.Consumer;

import org.grapple.core.ElementVisibility;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityFieldDefinition;
import org.grapple.schema.EntityFilterItemDefinition;
import org.grapple.schema.EntityJoinDefinition;

public interface EntityConfiguration<X> {

    Class<X> getEntityClass();

    String getName();

    String getDescription();

    void setDescription(String description);

    String getDeprecationReason();

    void setDeprecationReason(String deprecationReason);

    <T> EntityFieldDefinition<X, T> configureField(EntityField<X, T> field);

    <Y> EntityJoinDefinition<X, Y> configureJoin(EntityJoin<X, Y> join);

    <T> EntityFilterItemDefinition<X, T> configureFilter(TypeLiteral<T> fieldType, Consumer<EntityFilterItemDefinition<X, T>> consumer);

    void setName(String name);

    ElementVisibility getVisibility();

    void setVisibility(ElementVisibility visibility);
}
