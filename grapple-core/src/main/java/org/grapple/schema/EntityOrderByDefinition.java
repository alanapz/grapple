package org.grapple.schema;

import org.grapple.utils.Chainable;

public interface EntityOrderByDefinition<X> extends Chainable<EntityOrderByDefinition<X>> {

    EntityDefinition<X> getParent();

    String getTypeName();

    void setTypeName(String typeName);

}
