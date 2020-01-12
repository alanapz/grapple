package org.grapple.schema;

import java.lang.reflect.Method;

public class EntityQueryScannerCallback {

    public void entityNotFound(Method method, Class<?> entityClass) {
        // Called when a query couldn't be loaded as matching entity not found
    }

    public boolean acceptQuery(Method method) {
        // Called to accept or not the given method
        return true;
    }

    public void configureQuery(Method method, EntityQueryDefinition<?> queryDefinition) {

    }
}
