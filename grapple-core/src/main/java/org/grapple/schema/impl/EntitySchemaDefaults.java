package org.grapple.schema.impl;

import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLEnumType.newEnum;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLNonNull.nonNull;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ClassLiteral.classLiteral;

import java.time.Instant;
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
        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            typeBuilder.value(enumValue.name(), enumValue);
        }
        return typeBuilder.build();
    }

    static void addDefaultTypes(EntitySchemaImpl schema) {

        schema.addUnmanagedType(classLiteral(Instant.class), instantType -> {

            instantType.addDataFetcher("timestamp", environment -> (environment.<Instant> getSource()).toEpochMilli());
            instantType.addDataFetcher("utc", environment -> DateTimeUtils.formatDateTimeUtc(environment.getSource(), environment.getArgument("pattern")));
            instantType.addDataFetcher("local", environment -> DateTimeUtils.formatDateTimeLocal(environment.getSource(), schema.getTimeZone(), environment.getArgument("pattern")));

            instantType.setTypeBuilder(ctx -> newObject().name("Timestamp")
                    .field(newFieldDefinition()
                            .name("timestamp")
                            .type(nonNull(GraphQLLong))
                            .description("Milliseconds since UNIX epoch"))
                    .field(newFieldDefinition().name("utc").type(nonNull(GraphQLString)).argument(newArgument()
                            .name("pattern")
                            .type(GraphQLString)
                            .description("eg: EE YYYY-MM-dd HH:mm | iso | rfc1123")
                            .build()))
                    .field(newFieldDefinition().name("local").type(nonNull(GraphQLString)).argument(newArgument()
                            .name("pattern")
                            .type(GraphQLString)
                            .description("eg: EE YYYY-MM-dd HH:mm | iso | rfc1123")
                            .build()))
                    .build());
        });
    }
}
