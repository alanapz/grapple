package org.grapple.schema;

import org.grapple.reflect.TypeLiteral;

public interface EntityDefaultNameGenerator {

    String generateFieldFilterEntityName(TypeLiteral<?> fieldType);

    String generateContainerEntityName(EntityDefinition<?> entity);

    String generateFilterEntityName(EntityDefinition<?> entity);

    String generateOrderByEntityName(EntityDefinition<?> entity);

}
