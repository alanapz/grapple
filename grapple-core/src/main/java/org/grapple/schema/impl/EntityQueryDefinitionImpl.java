package org.grapple.schema.impl;

import org.grapple.schema.EntityQueryDefinition;

interface EntityQueryDefinitionImpl<X> extends EntityQueryDefinition<X> {

    EntityDefinitionImpl<X> getEntity();

    void build(SchemaBuilderContext ctx);

}

