package org.grapple.schema;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.grapple.query.EntityRoot;
import org.grapple.query.RootFetchSet;

public class QueryResolverFactory {

    private QueryResolverFactory() {

    }

    public static <X> EntityQueryResolver<X> defaultQueryResolver(EntityManagerFactory entityManagerFactory, EntityRoot<X> entityRoot) {
        return defaultQueryResolver(entityManagerFactory, entityRoot, (fetchSet, queryParameters) -> {});
    }

    public interface DefaultQueryResolverCallback<X> {

        void accept(RootFetchSet<X> fetchSet, Map<String, Object> queryParameters);

    }

    public static <X> EntityQueryResolver<X> defaultQueryResolver(EntityManagerFactory entityManagerFactory, EntityRoot<X> entityRoot, DefaultQueryResolverCallback<X> queryCallback) {
        requireNonNull(entityManagerFactory, "entityManagerFactory");
        requireNonNull(entityRoot, "entityRoot");
        requireNonNull(queryCallback, "queryCallback");
        return (env, fetchSet, queryParameters) -> {
            final EntityManager entityManager = entityManagerFactory.createEntityManager();
            try {
                queryCallback.accept(fetchSet, queryParameters);
                return fetchSet.execute(entityManager, entityRoot);
            }
            finally {
                entityManager.close();
            }
        };
    }

}
