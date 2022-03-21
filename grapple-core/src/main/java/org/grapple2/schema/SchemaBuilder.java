/*
package org.grapple2.schema;

import static java.lang.String.format;

import java.util.Map;

import org.grapple.schema.impl.EntityDefinitionImpl;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple2.GrappleEntity;
import org.jetbrains.annotations.NotNull;

public class SchemaDefinition {

    private final Map<Class<?>, EntityDefinitionImpl<?>> entities = new NoDuplicatesMap<>();

    public <E> void addUnmanagedEntity(@NotNull Class<E> eClass) {
        final GrappleEntity grappleEntity = eClass.getDeclaredAnnotation(GrappleEntity.class);
        if (grappleEntity == null) {
            throw new IllegalArgumentException(format("'%s' is not annotated with @GrappleEntity", eClass.getName()));
        }
        if (!eClass.isInterface()) {
            throw new IllegalArgumentException(format("'%s' is not an interface", eClass.getName()));
        }
    }

}
*/