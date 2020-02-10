package org.grapple.schema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.grapple.query.EntityField;
import org.grapple.query.EntityJoin;

public class EntitySchemaScannerCallback {

    public boolean acceptEntity(Class<?> entityClass) {
        return true;
    }

    public void configureEntity(Class<?> entityClass, EntityDefinition<?> entityDefinition) {
        // Nothing to do here
    }

    public boolean acceptField(EntityField<?, ?> entityField) {
        return true;
    }

    public void configureField(Field field, EntityFieldDefinition<?, ?> fieldDefinition) {
        // Overridden by subclasses
    }

    public boolean acceptJoin(EntityJoin<?, ?> entityJoin) {
        return true;
    }

    public void configureJoin(Field field, EntityJoinDefinition<?, ?> joinDefinition) {
        // Overridden by subclasses
    }

    public <X, T> boolean acceptFilter(Method method, EntityDefinition<X> entity, EntityFilterItemBuilder<X, T> builder) {
        return true;
    }

    public boolean acceptQuery(EntityDefinition<?> entity, Method source) {
        // Called to accept or not the given method
        return true;
    }

    public void configureQuery(Method source, EntityQueryDefinition<?> queryDefinition) {
        // Overridden by subclasses
    }
}
