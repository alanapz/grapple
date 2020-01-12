//package org.grapple.schema;
//
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.util.Set;
//import graphql.schema.GraphQLArgument;
//import org.grapple.invoker.GrappleArgument;
//
//import static org.grapple.utils.Utils.stringCoalesce;
//
//public class ObjectBuilder {
//
//    public interface X extends QueryParameters<OutOfMemoryError> {
//
//        Set<Integer> contextIds();
//
//        @GrappleArgument(name = "userIds2")
//        Set<Integer> userIds();
//    }
//
//    private static GraphQLArgument buildArgumentFromMethod(TypeRegistry typeRegistry, Method method) {
//        final GrappleArgument argumentAnnotation = method.getAnnotation(GrappleArgument.class);
//        return GraphQLArgument.newArgument()
//                .name(stringCoalesce(argumentAnnotation != null ? argumentAnnotation.name() : null, method.getName()))
//                // .type(typeRegistry.getType(method.getName(), method.getGenericReturnType()))
//                .description(stringCoalesce(argumentAnnotation != null ? argumentAnnotation.description() : null, null))
//                .build();
//    }
//
//    public static  void main(String[] args) throws Exception {
//
//        for(Method m: X.class.getMethods())
//        {
//            System.out.println(m);
//
//
//
//            GraphQLArgument.newArgument()
//                    .name(m.getName())
//                    // .type()
//                    .defaultValue(null)
//                    .description(null)
//                    .value(null);
//        }
//
//
//        X instance = (X) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{ X.class }, new InvocationHandler() {
//
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        System.out.print(proxy.getClass());
//                        System.out.println(method);
//                        System.out.println(method == QueryParameters.class.getDeclaredMethod("query"));
//                        System.out.println(method.equals(QueryParameters.class.getDeclaredMethod("query")));
//                        return null;
//                    }
//                });
//
//        System.out.print(instance.query());
//        System.out.print(instance.contextIds());
//        System.out.print(instance.dataFetchingEnvironment());
//
////        objectBuilder.field(newFieldDefinition()
////                .name("listAllUsers")
////                .argument(GraphQLArgument.newArgument().name("filter").type(GraphQLTypeReference.typeRef("UserFilter")).build())
////                .argument(GraphQLArgument.newArgument().name("contextId").type(Scalars.GraphQLInt).build())
////                .type(GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef("UserResults"))));
//
//    }
//}
