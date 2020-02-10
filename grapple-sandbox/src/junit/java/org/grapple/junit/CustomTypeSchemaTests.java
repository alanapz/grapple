package org.grapple.junit;

import static graphql.schema.GraphQLTypeReference.typeRef;
import static org.grapple.query.EntityRootBuilder.entityRoot;
import static org.grapple.reflect.ClassLiteral.classLiteral;
import static org.grapple.utils.Utils.toSet;

import java.util.Optional;
import javax.persistence.EntityManager;
import app.CustomObject;
import app.Launch;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import org.grapple.invoker.GrappleQuery;
import org.grapple.query.Filters;
import org.grapple.query.QueryDefinitions;
import org.grapple.query.QueryResultList;
import org.grapple.query.QueryResultRow;
import org.grapple.query.RootFetchSet;
import org.grapple.schema.EntitySchema;
import org.grapple.schema.EntitySchemaResult;
import org.grapple.schema.EntitySchemaScannerCallback;
import org.grapple.schema.impl.EntitySchemaProvider;
import org.junit.jupiter.api.Test;
import sandbox.grapple.UserField;
import sandbox.grapple.UserPrivateMessageField;
import sandbox.grapple.entity.User;

public class CustomTypeSchemaTests extends SchemaTestSupport {

    @Test
    public void testCustomResolver() {

        final EntityManager entityManager = getEntityManager();

        final EntitySchema entitySchema = EntitySchemaProvider.newSchema();
        entitySchema.buildEntitySchemaScanner(new EntitySchemaScannerCallback()).apply(entitySchemaScanner -> {
            entitySchemaScanner.importDefinitions(QueryDefinitions.class, "app", "sandbox");
            entitySchemaScanner.importQueries(new Object() {

                @GrappleQuery
                public QueryResultList<User> listUsers(RootFetchSet<User> fetches) {
                    return fetches.execute(entityManager, entityRoot(User.class));
                }

                @GrappleQuery
                public QueryResultRow<User> getUserById(RootFetchSet<User> fetches, int userId) {
                    fetches.filter(Filters.isEqual(UserField.Id, userId));
                    return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult().orElse(null);
                }

                @GrappleQuery
                public Optional<QueryResultRow<User>> getUserByIdOrNull(RootFetchSet<User> fetches, int userId) {
                    fetches.filter(Filters.isEqual(UserField.Id, userId));
                    return fetches.execute(entityManager, entityRoot(User.class)).getUniqueResult();
                }
            });
        });

        entitySchema.addUnmanagedType(classLiteral(CustomObject.class), customObjectDefinition -> {
            customObjectDefinition.setTypeBuilder(ctx -> {
                final GraphQLObjectType.Builder objectType = new GraphQLObjectType.Builder();
                objectType.name("customObject");
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("value").type(Scalars.GraphQLString).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("pmType").type((GraphQLOutputType) ctx.buildTypeFor(UserPrivateMessageField.PMType.class)).build());
                return objectType.build();
            });
        });

        entitySchema.addUnmanagedType(classLiteral(UserField.CustomDetails.class), customObjectDefinition -> {
            customObjectDefinition.setTypeBuilder(ctx -> {
                final GraphQLObjectType.Builder objectType = new GraphQLObjectType.Builder();
                objectType.name("userCustomDetails");
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("username").type(Scalars.GraphQLString).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("username1").type(Scalars.GraphQLString).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("username2").type(Scalars.GraphQLString).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("username3").type(Scalars.GraphQLString).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("items").type(GraphQLList.list(typeRef("userCustomDetailsItem"))).build());
                return objectType.build();
            });
        });

        entitySchema.addUnmanagedType(classLiteral(UserField.CustomDetailsItem.class), customObjectDefinition -> {
            customObjectDefinition.setTypeBuilder(ctx -> {
                final GraphQLObjectType.Builder objectType = new GraphQLObjectType.Builder();
                objectType.name("userCustomDetailsItem");
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("value").type((GraphQLOutputType) ctx.buildTypeFor(UserField.CustomDetailsItemValue.class)).build());
                objectType.field(GraphQLFieldDefinition.newFieldDefinition().name("description").type(Scalars.GraphQLString).build());
                return objectType.build();
            });
        });


//        entitySchema.addUnmanagedType(objectType.build());
//        entitySchema.addTypeMapping(CustomObject.class, GraphQLTypeReference.typeRef("customObject"));

        System.out.println(entitySchema);
        final EntitySchemaResult generatedEntitySchema = Launch.buildGraphQL(entitySchema);
        final GraphQL graphQL = GraphQL.newGraphQL(generatedEntitySchema.getSchemaWithVisibility(toSet("xx", "yyy", "adminx"))).build();
        Launch.runQuery(graphQL, "query{ listUsers(count:1) { offset, count, total, results { id, userCustomDetails{username, username1, username2, username3, items { value, description } } } } }");
    }
}
