package org.grapple.schema;

import org.grapple.utils.Chainable;

public interface EntityJoinDefinition extends Chainable<EntityJoinDefinition> {

    EntityJoinDefinition setName(String name);

    EntityJoinDefinition setDescription(String description);

    EntityJoinDefinition setIsDeprecated(boolean deprecated);

    EntityJoinDefinition setDeprecationReason(String deprecationReason);

}
