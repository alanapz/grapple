package org.grapple.schema;

import org.grapple.reflect.TypeLiteral;

public abstract class EntitySchemaListener {

    public boolean acceptFieldFilter(TypeLiteral<?> fieldType) {
        return true;
    }

    public void configureFieldFilter(FieldFilterDefinition<?> fieldFilter) {
        // Nothing to do here by default
    }

    public boolean acceptEntityQuery(EntityQueryDefinition<?> entityQuery) {
        return true;
    }

    public boolean acceptEntityOrderBy(EntityDefinition<?> entity) {
        return true;
    }

    public void configureEntityOrderBy(EntityOrderByDefinition<?> orderBy) {
        // Nothing to do here by default
    }
}
