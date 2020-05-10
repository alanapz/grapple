package org.grapple.schema;

import java.lang.annotation.Annotation;
import org.grapple.core.Chainable;

public interface EntitySchemaScanner extends Chainable<EntitySchemaScanner> {

    void importDefinitions(Class<? extends Annotation> annotation, String... packageNames);

    void importDefinitions(Class<?> definitionsClass);

    void importOperations(Object instance);

    void importOperations(Object instance, Class<?> instanceClass);

}
