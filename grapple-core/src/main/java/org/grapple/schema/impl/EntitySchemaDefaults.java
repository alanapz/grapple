package org.grapple.schema.impl;

import static graphql.schema.GraphQLEnumType.newEnum;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import graphql.schema.GraphQLEnumType;
import org.grapple.reflect.ClassLiteral;
import org.grapple.reflect.TypeLiteral;
import org.grapple.schema.EntityDefaultNameGenerator;
import org.grapple.schema.EntityDefinition;

final class EntitySchemaDefaults {

    private EntitySchemaDefaults() {

    }

    static EntityDefaultNameGenerator defaultNameGenerator() {
        return new EntityDefaultNameGenerator() {

            @Override
            public String generateFieldFilterEntityName(TypeLiteral<?> fieldType) {
                requireNonNull(fieldType, "fieldType");
                return format("%sFilter", ((ClassLiteral<?>) fieldType).getType().getSimpleName());
            }

            @Override
            public String generateContainerEntityName(EntityDefinition<?> entity) {
                requireNonNull(entity, "entity");
                return format("%sResults", entity.getName());
            }

            @Override
            public String generateFilterEntityName(EntityDefinition<?> entity) {
                requireNonNull(entity, "entity");
                return format("%sFilter", entity.getName());
            }

            @Override
            public String generateOrderByEntityName(EntityDefinition<?> entity) {
                requireNonNull(entity, "entity");
                return format("%sOrderBy", entity.getName());
            }
        };
    }

    static GraphQLEnumType buildEnumTypeForClass(Class<? extends Enum<?>> enumClass) {
        requireNonNull(enumClass, "enumClass");
        final GraphQLEnumType.Builder typeBuilder = newEnum().name(enumClass.getSimpleName());
        for (Enum<?> enumValue: enumClass.getEnumConstants()) {
            typeBuilder.value(enumValue.name(), enumValue);
        }
        return typeBuilder.build();
    }
}
