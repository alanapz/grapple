package org.grapple.query;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedElement;
import javax.persistence.metamodel.SingularAttribute;

final class QueryUtils {

    private QueryUtils() {

    }

    static String getDefaultDescription(SingularAttribute<?, ?> attribute) {
        requireNonNull(attribute, "attribute");
        if (!(attribute.getJavaMember() instanceof AnnotatedElement)) {
            return null;
        }
        // Default description is simply null now
        return null;
    }

    static String getDefaultDeprecationReason(SingularAttribute<?, ?> attribute) {
        requireNonNull(attribute, "attribute");
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
