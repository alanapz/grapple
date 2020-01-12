package org.grapple.schema;

import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;

public class EntityDefinitionScannerCallback {

    public boolean scanDefinitions(Class<?> definitionsClass) {
        return true;
    }

    public boolean acceptEntity(Class<?> entityClass) {
        return true;
    }

    public void configureEntity(EntityDefinition<?> entity) {
        // Nothing to do here
    }

    public <X, T> boolean acceptField(EntityDefinition<X> entity, EntityField<X, T> field) {
        return true;
    }

    public <X, T> void configureField(EntityFieldDefinition<X, T> fieldDefinition) {
        // Nothing to do here
    }

    public <X, Y> boolean acceptJoin(EntityDefinition<X> entity, EntityJoin<X, Y> join) {
        return true;
    }

    public <X, Y> void configureJoin(EntityJoinDefinition<X, Y> joinDefinition) {
        // Nothing to do here
    }
}
