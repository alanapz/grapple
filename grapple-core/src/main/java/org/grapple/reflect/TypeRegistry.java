package org.grapple.reflect;

import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
import static graphql.schema.GraphQLList.list;
import static graphql.schema.GraphQLNonNull.nonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.grapple.reflect.ReflectUtils.wrapNonNullIfNecessary;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import graphql.Scalars;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.idl.SchemaPrinter;
import org.grapple.scalars.GrappleScalars;
import org.grapple.utils.NoDuplicatesMap;

public class TypeRegistry {

    enum Reason
    {
        ACCESS_DENIED,
        TOO_MANY_POTATOES
    }

    @JsonTypeName("superUserAction")
    enum UserAction
    {
        READ,
        WRITE,
        EXECUTE,
        DELETE
    }

    public class ResultClass<X> {

        @JsonProperty("allowed")
        public Set<X> allowed;

        @JsonProperty("notAllowed")
        public Set<ResultItem<X>> notAllowed;

        @JsonProperty("notAllowed2")
        public Set<ResultItem<?>> notAllowed2;

        @JsonProperty("obj")
        public Object object;

        @JsonProperty("instant")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Instant instant;
    }

    public class ResultItem<X> {

        public Set<X> refused;

        public Set<Reason> reason;

    }

    public class UserResultClass extends ResultClass<UserAction> {


    }

    private static final Map<Type, GraphQLOutputType> defaultTypeMappings = new HashMap<>();

    static final ObjectMapper objectMapper = new ObjectMapper();

    private static Map<JavaType, BeanDescription> beanDescriptionCache = new NoDuplicatesMap<>();

    private Map<JavaType, GraphQLInputObjectType> inputTypeCache = new NoDuplicatesMap<>();

    private Map<JavaType, String> inputTypeNameCache = new NoDuplicatesMap<>();

    private Set<JavaType> inputTypesToProcess = new HashSet<>();

    private static final SchemaPrinter defaultSchemaPrinter = new SchemaPrinter(SchemaPrinter.Options.defaultOptions()
            .includeScalarTypes(true)
            .includeExtendedScalarTypes(true)
            .includeIntrospectionTypes(true)
            .includeDirectives(true)
            .includeSchemaDefinition(true));


    private TypeFactoryCallback typeFactoryCallback;

    public static void main(String[] args) {

        TypeRegistry tr = new TypeRegistry();
        System.out.println(tr.buildInputTypeObject(UserResultClass.class));
        System.out.println(tr.inputTypesToProcess);
        System.out.println(tr.buildInputTypeObject(Instant.class));

        Test1 test1 = new Test1();
        test1.longObject = 123L;
        test1.longPrimitive = 999;
        test1.listOfListOfTest2 = Arrays.asList(Arrays.asList(new Test2()));

        System.out.println(new ObjectMapper().convertValue(test1, Map.class));



    }

    private GraphQLInputObjectType buildInputTypeObject(Class<?> clazz) {
        requireNonNull(clazz, "clazz");
        GraphQLInputObjectType.Builder builder  = new GraphQLInputObjectType.Builder();

        JavaType javaType = objectMapper.getTypeFactory().constructType(clazz);

        final BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(javaType);


        GraphQLInputObjectType finalType = buildInputTypeObject(javaType);
        return finalType;

            }

    private GraphQLInputObjectType buildInputTypeObject(JavaType javaType) {
        requireNonNull(javaType, "javaType");
        return inputTypeCache.computeIfAbsent(javaType, unused -> {
            final BeanDescription beanDescription = objectMapper.getSerializationConfig().introspect(javaType);
            final GraphQLInputObjectType.Builder inputObjectBuilder = newInputObject();
            inputObjectBuilder.name(getInputTypeName(javaType));
            // TODO: Description + Deprecated ?
            for (BeanPropertyDefinition property: beanDescription.findProperties()) {
                final GraphQLInputObjectField inputObjectField = buildInputTypeProperty(property);
                if (inputObjectField != null) {
                    inputObjectBuilder.field(inputObjectField);
                }
            }
            return inputObjectBuilder.build();
        });
    }

    private GraphQLInputObjectField buildInputTypeProperty(BeanPropertyDefinition property) {
        requireNonNull(property, "property");
        final JavaType javaType = property.getPrimaryType();
        final GraphQLInputType graphQLInputType = (GraphQLInputType) resolveInputTypeFor(javaType);
        if (graphQLInputType == null) {
            throw new UnmappableTypeException(format("Couldn't map input type for: %s", javaType));
        }
        final GraphQLInputObjectField.Builder inputFieldBuilder = newInputObjectField();
        inputFieldBuilder.name(property.getName());
        inputFieldBuilder.description(property.getMetadata().getDescription());
        inputFieldBuilder.type((GraphQLInputType) wrapNonNullIfNecessary(graphQLInputType, property.isRequired()));
        return inputFieldBuilder.build();
    }

    // Returns the corresponding GraphQL type
    // Primitives and collection types are returned directly
    // If we encounter an unmapped entity type, add to
    private GraphQLType resolveInputTypeFor(JavaType javaType) {
        requireNonNull(javaType, "javaType");
        final Class<?> clazz = javaType.getRawClass();
        // Check if type already exists
        if (defaultTypeMappings.containsKey(clazz)) {
            return defaultTypeMappings.get(clazz);
        }
        // Object and dictionary types are treated as untyped objects
        if (javaType.isJavaLangObject() || javaType.isMapLikeType()) {
            return ExtendedScalars.Object;
        }
        // Recurse array types
        if (javaType.isCollectionLikeType()) {
            final GraphQLType contentType = resolveInputTypeFor(javaType.getContentType());
            return (contentType != null ? list(contentType) : null);
        }
        // We are a normal object type - add type to list to process and generate typeref
        inputTypesToProcess.add(javaType);
        return GraphQLTypeReference.typeRef(getInputTypeName(javaType));
    }

    private String getInputTypeName(JavaType javaType) {
        requireNonNull(javaType, "javaType");
        return inputTypeNameCache.computeIfAbsent(javaType, unused -> {
            // Enum classes are both input and output, no need to add xxxInput suffix
            if (javaType.isEnumType()) {
                return javaType.getRawClass().getSimpleName();
            }
            if (!javaType.hasGenericTypes()) {
                final BeanDescription beanDescription = objectMapper.getDeserializationConfig().introspect(javaType);
                if (beanDescription.getClassAnnotations().has(JsonTypeName.class)) {
                    return beanDescription.getClassAnnotations().get(JsonTypeName.class).value();
                }
                return format("%sInput", javaType.getRawClass().getSimpleName());
            }
            final StringBuilder buffer = new StringBuilder();
            for (JavaType typeParameter: javaType.getBindings().getTypeParameters()) {
                buffer.append(getInputTypeName(typeParameter));
            }
            buffer.append(format("%sInput", javaType.getRawClass().getSimpleName()));
            return buffer.toString();
        });
    }

    @JsonTypeName("zebra2")
    public static class Test1 {

        @JsonProperty(value = "lO", required = true)
        @JsonPropertyDescription("description1")
        public Long longObject;

        @JsonProperty("lP")
        @JsonPropertyDescription("description2")
        public long longPrimitive;
//
//        @JsonProperty("values")
//        @JsonIgnore
//        public List<String> valuesx;

//        @JsonProperty("value2")
//        public List<String> values2x;
//
        @JsonProperty("l2")
        public List<List<Test2>> listOfListOfTest2;

//        @JsonIgnore
//        String ignore;
    }

    @JsonTypeName("zebra")
    public static class Test2 {

        public Set<Test3<String>> valuesOfTest3;
    }

    public static class ResultList<T> {

        public ResultList<T> parent;

        public List<T> values;

        public List<String> valuesAsString;
    }


    @JsonTypeName("superTypoe")
    public class Test3<A> {

        public Set<A> values;
    }

    static {
        // Primitives
        defaultTypeMappings.put(boolean.class, nonNull(Scalars.GraphQLBoolean));
        defaultTypeMappings.put(byte.class, nonNull(Scalars.GraphQLByte));
        defaultTypeMappings.put(short.class, nonNull(Scalars.GraphQLShort));
        defaultTypeMappings.put(int.class, nonNull(Scalars.GraphQLInt));
        defaultTypeMappings.put(long.class, nonNull(Scalars.GraphQLLong));
        defaultTypeMappings.put(char.class, nonNull(Scalars.GraphQLChar));
        defaultTypeMappings.put(float.class, nonNull(Scalars.GraphQLFloat));
        defaultTypeMappings.put(double.class, nonNull(Scalars.GraphQLFloat)); // No "double" type

        // Primitive object wrappers
        defaultTypeMappings.put(Boolean.class, Scalars.GraphQLBoolean);
        defaultTypeMappings.put(Byte.class, Scalars.GraphQLByte);
        defaultTypeMappings.put(Short.class, Scalars.GraphQLShort);
        defaultTypeMappings.put(Integer.class, Scalars.GraphQLInt);
        defaultTypeMappings.put(Long.class, Scalars.GraphQLLong);
        defaultTypeMappings.put(Character.class, Scalars.GraphQLChar);
        defaultTypeMappings.put(Float.class, Scalars.GraphQLFloat);
        defaultTypeMappings.put(Double.class, Scalars.GraphQLFloat); // No "Double" type

        defaultTypeMappings.put(String.class, Scalars.GraphQLString);

        defaultTypeMappings.put(UUID.class, Scalars.GraphQLID);

        defaultTypeMappings.put(BigDecimal.class, Scalars.GraphQLBigDecimal);
        defaultTypeMappings.put(BigInteger.class, Scalars.GraphQLBigInteger);

        defaultTypeMappings.put(LocalDate.class, ExtendedScalars.Date);
        defaultTypeMappings.put(OffsetDateTime.class, ExtendedScalars.DateTime);
        defaultTypeMappings.put(Object.class, ExtendedScalars.Object);
        defaultTypeMappings.put(Locale.class, ExtendedScalars.Locale);
        defaultTypeMappings.put(OffsetTime.class, ExtendedScalars.Time);
        defaultTypeMappings.put(URL.class, ExtendedScalars.Url);

        defaultTypeMappings.put(YearMonth.class, GrappleScalars.YearMonthScalar);
    }
}
