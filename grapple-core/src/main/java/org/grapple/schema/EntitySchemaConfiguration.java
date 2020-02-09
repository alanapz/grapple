package org.grapple.schema;

import java.util.Set;
import graphql.schema.GraphQLSchema;
import org.grapple.core.Chainable;
import org.grapple.reflect.TypeConverter;

public interface EntitySchemaConfiguration extends Chainable<EntitySchemaConfiguration> {

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
