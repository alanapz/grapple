/*
package org.grapple2.schema;

import static org.grapple.reflect.ReflectUtils.getDeclaredAnnotation;

import java.lang.reflect.Method;

import org.grapple.core.Validatable;
import org.grapple.utils.NoDuplicatesMap;
import org.grapple2.core.Description;
import org.grapple2.core.Name;

public final class UnmanagedEntityDefinition<X> implements Validatable {

    private final SchemaDefinition schema;

    private final Class<X> dataClass;

    private final Class<?> modelClass;

    private final String name;

    private String description;

    private final Set<String, UnmanagedFieldDefinition<X, ?>> fields = new NoDuplicatesMap<>();

    private final Set<String, UnmanagedFieldDefinition<X, ?>> joins = new NoDuplicatesMap<>();

    public UnmanagedEntityDefinition(SchemaDefinition schema, Class<?> entityClass, Class<X> dataClass, Class<?> modelClass) {
        this.schema = schema;
        this.dataClass = dataClass;
        this.modelClass = modelClass;
        this.name = getDeclaredAnnotation(modelClass, Name.class).map(Name::value).orElse(modelClass.getSimpleName());
        this.description = getDeclaredAnnotation(modelClass, Description.class).map(Description::value).orElse(null);
        for (Method m: modelClass.getMethods()) {

        }
    }

    private void addMethod(Method method) {
        UnmanagedFieldDefinition fieldDefinition = new UnmanagedFieldDefinition(this, method, dataClass);
    }

    @Override
    public void validate() {
        // Make sure names are unique


    }
}
*/