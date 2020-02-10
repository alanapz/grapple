package org.grapple.schema;

import java.util.function.Consumer;
import org.grapple.core.Chainable;
import org.grapple.reflect.TypeConverter;
import org.grapple.reflect.TypeLiteral;

public interface EntitySchema extends Chainable<EntitySchema> {

    void addSchemaListener(EntitySchemaListener listener);

    void addEntityQueryExecutionListener(EntityQueryExecutionListener listener);

    <X> EntityDefinition<X> getEntity(Class<X> entityClass);

    <X> EntityDefinition<X> addEntity(Class<X> entityClass);

    EnumTypeBuilder getEnumTypeBuilder();

    void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder);

    EntityDefaultNameGenerator getEntityDefaultNameGenerator();

    void setEntityDefaultNameGenerator(EntityDefaultNameGenerator entityDefaultNameGenerator);

    EntitySchemaScanner buildEntitySchemaScanner(EntitySchemaScannerCallback scannerCallback);

    TypeConverter getTypeConverter();

    void addUnmanagedQuery(String queryAlias, Consumer<UnmanagedQueryDefinition> consumer);

    <T> void addUnmanagedType(TypeLiteral<T> type, Consumer<UnmanagedTypeDefinition<T>> consumer);

//    void addTypeAlias(String typeAliasName, GraphQLType type);

//    EntitySchema addCustomType(String typeAlias, GraphQLType type);

    // EntitySchema addDefaultType(Type javaType, GraphQLType type);

    // <X> void addEntityQueryInvoker(String name, Class<X> entityClass, Class<QueryParameters<X>> invokerParams, QueryResolver<X> queryResolver);

    EntitySchemaResult generate();
}
