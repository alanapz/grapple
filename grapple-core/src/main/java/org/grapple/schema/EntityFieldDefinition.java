package org.grapple.schema;

import org.grapple.utils.Chainable;

public interface EntityFieldDefinition extends Chainable<EntityFieldDefinition> {

    EntityFieldDefinition setName(String name);

    EntityFieldDefinition setDescription(String description);

    EntityFieldDefinition setIsDeprecated(boolean deprecated);

    EntityFieldDefinition setDeprecationReason(String deprecationReason);

}
