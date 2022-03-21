/*
package org.grapple2.schema;

import static org.grapple.reflect.ReflectUtils.getDeclaredAnnotation;

import java.lang.reflect.Method;

import org.grapple2.core.Description;
import org.grapple2.core.FieldComplexity;
import org.jetbrains.annotations.NotNull;

public interface UnmanagedFieldResolver<X, T> {

    T get(NonQueryFieldResolverContext<X> ctx);

    private final Class<?> eClass;

    private final Method method;

    public String description;

    public Integer complexity;

    NonQueryFieldResolver(@NotNull Class<?> eClass, @NotNull Method method) {
        this.eClass = eClass;
        this.method = method;
        this.description = getDeclaredAnnotation(method, Description.class).map(Description::value).orElse(null);
        this.complexity = getDeclaredAnnotation(method, FieldComplexity.class).map(FieldComplexity::value).orElse(null);
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.description = description;
    }

    public void setResolver()
    {

    }
}
*/