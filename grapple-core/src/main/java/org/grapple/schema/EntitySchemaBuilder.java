package org.grapple.schema;

public final class EntitySchemaBuilder {

    private EntitySchemaBuilder() {

    }

    public static EntitySchema newSchema() {
        return new EntitySchemaImpl();
    }
}
