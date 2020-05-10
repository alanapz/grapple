package org.grapple.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import org.grapple.schema.EntityQueryType;
import org.grapple.utils.NoDuplicatesMap;

public final class EntityQueryMetadata {

    private EntityQueryMetadata() {

    }

    public enum EntityQueryMethodType {
        LIST,
        ROW,
        OPTIONAL_ROW
    }

    public static final class EntityQueryParameterMetadata {

        public String name;

        public int index;

        public Type type;

        public String description;

        public String deprecationReason;

        public boolean required;
    }

    public static final class EntityQueryMethodMetadata<X> {

        public final Class<X> entityClass;

        public final Method method;

        public final EntityQueryMethodType methodType; // "real" query type (list / scalar / optional scalar)

        public final String queryName;

        public final EntityQueryType queryType; // Adapted query type

        public final String description;

        public String deprecationReason;

        public final Map<String, EntityQueryParameterMetadata> parameters = new NoDuplicatesMap<>();

        public int fetchSetParameterIndex; // Index of fetch set in method params array

        public EntityQueryMethodMetadata(Class<X> entityClass, Method method, EntityQueryMethodType methodType, String queryName, EntityQueryType queryType, String description) {
            this.entityClass = requireNonNull(entityClass, "entityClass");
            this.method = requireNonNull(method, "method");
            this.methodType = requireNonNull(methodType, "methodType");
            this.queryName = requireNonNull(queryName, "queryName");
            this.queryType = requireNonNull(queryType, "queryType");
            this.description = description;
        }
    }
}
