package org.grapple.schema.impl;

import org.grapple.schema.EntitySchema;

public final class EntitySchemaProvider {

    private EntitySchemaProvider() {

    }

    public static EntitySchema newSchema() {
        return new EntitySchemaImpl();
    }
}
