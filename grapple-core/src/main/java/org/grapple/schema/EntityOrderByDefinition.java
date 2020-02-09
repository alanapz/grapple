package org.grapple.schema;

import org.grapple.core.Chainable;

public interface EntityOrderByDefinition<X> extends Chainable<EntityOrderByDefinition<X>> {

    EntityDefinition<X> getParent();

    String getTypeName();

    void setTypeName(String typeName);

}
