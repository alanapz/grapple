package org.grapple.schema;

import java.util.Set;
import graphql.schema.GraphQLSchema;
import org.grapple.reflect.TypeConverter;
import org.grapple.utils.Chainable;

public interface EntitySchema extends Chainable<EntitySchema> {

    void addSchemaListener(EntitySchemaListener listener);

    <X> EntityDefinition<X> getEntity(Class<X> entityClass);

    <X> EntityDefinition<X> addEntity(Class<X> entityClass);

    EnumTypeBuilder getEnumTypeBuilder();

    void setEnumTypeBuilder(EnumTypeBuilder enumTypeBuilder);

    EntityDefaultNameGenerator getEntityDefaultNameGenerator();

    void setEntityDefaultNameGenerator(EntityDefaultNameGenerator entityDefaultNameGenerator);

    void importDefinitions(Set<String> packageNames, EntityDefinitionScannerCallback scannerCallback);

    void importQueries(Object instance, EntityQueryScannerCallback scannerCallback);

    TypeConverter getTypeConverter();


//    void addTypeAlias(String typeAliasName, GraphQLType type);


//    EntitySchema addCustomType(String typeAlias, GraphQLType type);

    // EntitySchema addDefaultType(Type javaType, GraphQLType type);

    // <X> void addEntityQueryInvoker(String name, Class<X> entityClass, Class<QueryParameters<X>> invokerParams, QueryResolver<X> queryResolver);

    GraphQLSchema generate();
}
