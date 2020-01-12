package org.grapple.schema;

import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.Chainable;

public interface EntityQueryDefinitionParameter<T> extends Chainable<EntityQueryDefinitionParameter<T>> {

    TypeLiteral<T> getType();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    boolean isRequired();

    void setRequired(boolean required);

    String getTypeAlias();

    void setTypeAlias(String typeAlias);

}
