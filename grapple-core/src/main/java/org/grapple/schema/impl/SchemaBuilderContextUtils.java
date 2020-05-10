package org.grapple.schema.impl;

import static java.util.Objects.requireNonNull;
import static org.grapple.utils.Utils.readOnlyCopy;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import graphql.schema.GraphQLEnumType;
import org.grapple.schema.EnumTypeBuilder;
import org.grapple.utils.NoDuplicatesMap;

final class SchemaBuilderContextUtils {

    public interface EnumTypeCache {

        GraphQLEnumType buildIfEnumType(Type type);

        Set<GraphQLEnumType> getAllEnumTypes();

    }

    static EnumTypeCache enumTypeCache(SchemaBuilderContext schemaBuilderContext, EnumTypeBuilder enumTypeBuilder) {
        requireNonNull(schemaBuilderContext, "schemaBuilderContext");
        requireNonNull(enumTypeBuilder, "enumTypeBuilder");
        return new EnumTypeCacheImpl(enumTypeBuilder);
    }

    private static final class EnumTypeCacheImpl implements EnumTypeCache {

        private final EnumTypeBuilder enumTypeBuilder;

        private final Map<Class<?>, GraphQLEnumType> enumTypeMappings = new NoDuplicatesMap<>();

        private EnumTypeCacheImpl(EnumTypeBuilder enumTypeBuilder) {
            this.enumTypeBuilder = requireNonNull(enumTypeBuilder, "enumTypeBuilder");
        }

        @Override
        public GraphQLEnumType buildIfEnumType(Type type) {
            requireNonNull(type, "type");
            if (!(type instanceof Class<?>)) {
                return null;
            }
            final Class<?> klazz = (Class<?>) type;
            // If type is an enum, look to see whether we have already been created (we need to create a cache to make sure we don't create multiple enum classes)
            if (!Enum.class.isAssignableFrom(klazz)) {
                return null;
            }
            if (enumTypeMappings.containsKey(klazz)) {
                return enumTypeMappings.get(klazz);
            }
            final @SuppressWarnings("unchecked") GraphQLEnumType enumType = enumTypeBuilder.buildEnumType((Class<? extends Enum<?>>) klazz.asSubclass(Enum.class));
            if (enumType == null) {
                return null;
            }
            enumTypeMappings.put(klazz, enumType);
            return enumType;
        }

        @Override
        public Set<GraphQLEnumType> getAllEnumTypes() {
            return readOnlyCopy(new HashSet<>(enumTypeMappings.values()));
        }
    }
}
