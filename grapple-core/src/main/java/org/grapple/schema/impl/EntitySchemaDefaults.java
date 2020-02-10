package org.grapple.schema.impl;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLTypeReference.typeRef;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import graphql.Scalars;
import graphql.schema.DataFetcher;
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

    static void addDefaultTypes(EntitySchemaImpl schema) {

        schema.addUnmanagedType(newObject().name("Timestamp")
                .field(newFieldDefinition().name("epochSeconds").type(nonNull(Scalars.GraphQLLong)))
                .field(newFieldDefinition().name("utc").type(nonNull(Scalars.GraphQLString)))
                .field(newFieldDefinition().name("local").type(nonNull(Scalars.GraphQLString)).argument(newArgument()
                        .name("pattern")
                        .type(Scalars.GraphQLString)
                        .description("eg: EE YYYY-MM-dd HH:mm")
                        .build()))
                .build());

        schema.addUnmanagedDataFetcher("Timestamp", "epochSeconds", (DataFetcher<Long>) environment -> {
            final Instant source = environment.getSource();
            return source.getEpochSecond();
        });

        schema.addUnmanagedDataFetcher("Timestamp", "utc", (DataFetcher<String>) environment -> {
            final Instant source = environment.getSource();
            return source.toString();
        });

        schema.addUnmanagedDataFetcher("Timestamp", "local", (DataFetcher<String>) environment -> {
            final Instant source = environment.getSource();
            final LocalDateTime localDateTime = LocalDateTime.ofInstant(source, ZoneId.systemDefault());
            final String pattern = environment.getArgument("pattern");
            if (pattern == null) {
                return localDateTime.toString();
            }
            return DateTimeFormatter.ofPattern(pattern).format(localDateTime);
        });

        schema.addTypeMapping(Instant.class, typeRef("Timestamp"));
    }
}
