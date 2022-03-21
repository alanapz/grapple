/*
package org.grapple2.schema;

import static org.grapple.reflect.ReflectUtils.getDeclaredAnnotation;

import java.lang.reflect.Method;

import org.grapple2.core.Description;
import org.grapple2.core.FieldComplexity;
import org.grapple2.core.Name;
import org.jetbrains.annotations.NotNull;

public final class UnmanagedJoinDefinition<X, T> {

    private final UnmanagedEntityDefinition<X> parent;

    private final Method method;

    private final String name;

    private String description;

    private Integer complexity;
    UnmanagedJoinDefinition(@NotNull UnmanagedEntityDefinition<X> parent, @NotNull Method method) {
        this.parent = parent;
        this.method = method;
        this.name = getDeclaredAnnotation(method, Name.class).map(Name::value).orElse(method.getName());
        this.description = getDeclaredAnnotation(method, Description.class).map(Description::value).orElse(null);
        this.complexity = getDeclaredAnnotation(method, FieldComplexity.class).map(FieldComplexity::value).orElse(null);
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

    public void setResolver()
    {

    }
}
*/