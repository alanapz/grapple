/*
package org.grapple2.schema;

import static org.grapple.reflect.ReflectUtils.getDeclaredAnnotation;

import java.lang.reflect.Method;

import org.grapple.core.ElementVisibility;
import org.grapple.reflect.ReflectUtils;
import org.grapple.reflect.TypeLiteral;
import org.grapple.utils.UnexpectedException;
import org.grapple2.core.Description;
import org.grapple2.core.FieldComplexity;
import org.grapple2.core.FieldVisibility;
import org.grapple2.core.Name;
import org.grapple2.core.Optional;
import org.jetbrains.annotations.NotNull;

public final class UnmanagedFieldDefinition<X, T> {

    private final UnmanagedEntityDefinition<X> parent;

    private final Method method;

    private TypeLiteral<T> resultType;

    private String name;

    private String description;

    private Integer complexity;

    private Boolean optional;

    private FieldVisibility visibility;

    UnmanagedFieldDefinition(@NotNull UnmanagedEntityDefinition<X> parent, @NotNull Method method) {
        this.parent = parent;
        this.method = method;
        this.resultType = ReflectUtils.typeLiteral(method.getReturnType());
        this.name = getDeclaredAnnotation(method, Name.class).map(Name::value).orElse(method.getName());
        this.description = getDeclaredAnnotation(method, Description.class).map(Description::value).orElse(null);
        this.complexity = getDeclaredAnnotation(method, FieldComplexity.class).map(FieldComplexity::value).orElse(null);
        this.optional = getDeclaredAnnotation(method, Optional.class).map(Optional::value).orElse(null);
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    Integer getComplexity() {
        return complexity;
    }

    public void setComplexity(Integer complexity) {
        this.complexity = complexity;
    }

    public Boolean isOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public void setResolver() {

    }

    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("name null");
        }
        if (resultType.isPrimitiveType() && Boolean.TRUE.equals(optional)) {
            throw new IllegalArgumentException("primitive types cannot be optional");
        }
        if (resultType.isOptional() && Boolean.FALSE.equals(optional)) {
            throw new IllegalArgumentException("optional types must be optional");
        }

        // Make sure are not an entity type !
        if (parent.getSchema().getEntityFor(resolveResultType()) != null) {
            throw new UnexpectedException(format("Field: %s.%s is an entity type (must use join instead of field)", entity.getEntityClass().getName(), field.getName()));
        }

    }

    public boolean resolveOptional() {
        if (optional != null) {
            return optional;
        }
        if (resultType.isPrimitiveType()) {
            return false;
        }
        if (resultType.isOptional()) {
            return true;
        }
        //
        return false;
    }

    public boolean resolveResultType() {

    }
}
*/