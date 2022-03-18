package org.grapple.query;

import java.lang.reflect.AnnotatedElement;
import javax.persistence.metamodel.SingularAttribute;

import org.jetbrains.annotations.NotNull;

final class QueryUtils {

    private QueryUtils() {

    }

    static String getDefaultDescription(@NotNull SingularAttribute<?, ?> attribute) {
        if (!(attribute.getJavaMember() instanceof AnnotatedElement)) {
            return null;
        }
        // Default description is simply null now
        return null;
    }

    static String getDefaultDeprecationReason(@NotNull SingularAttribute<?, ?> attribute) {
        if (!(attribute.getJavaMember() instanceof AnnotatedElement)) {
            return null;
        }
        final Deprecated deprecated = ((AnnotatedElement) attribute.getJavaMember()).getAnnotation(Deprecated.class);
        if (deprecated == null) {
            return null;
        }
        return "Deprecated";
    }
}
