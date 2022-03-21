package org.grapple.schema.impl;

import org.grapple.query.QueryProvider;
import org.grapple.schema.EntitySchema;

import org.jetbrains.annotations.NotNull;

public final class EntitySchemaProvider {

    private EntitySchemaProvider() {

    }

    public static EntitySchema newSchema(@NotNull QueryProvider queryProvider) {
        return new EntitySchemaImpl(queryProvider);
    }
}
