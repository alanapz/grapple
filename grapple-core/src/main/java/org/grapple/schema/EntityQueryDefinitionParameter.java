package org.grapple.schema;

import org.grapple.core.Chainable;
import org.grapple.reflect.TypeLiteral;

public interface EntityQueryDefinitionParameter<T> extends Chainable<EntityQueryDefinitionParameter<T>> {

    TypeLiteral<T> getType();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    boolean isRequired();

    void setRequired(boolean required);

}
