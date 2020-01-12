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
        // Default description is simply column name
        // final Column column = ((AnnotatedElement) attribute.getJavaMember()).getDeclaredAnnotation(Column.class);
        // if (column == null) {
        // return null;
        // }
        // return column.name();
    }

    static boolean isDefaultDeprecated(SingularAttribute<?, ?> attribute) {
        requireNonNull(attribute, "attribute");
        if (!(attribute.getJavaMember() instanceof AnnotatedElement)) {
            return false;
        }
        return ((AnnotatedElement) attribute.getJavaMember()).getDeclaredAnnotation(Deprecated.class) != null;
    }
}
