package org.grapple.resolver;

import java.util.function.Supplier;
import javax.persistence.EntityManager;
import org.grapple.query.EntityRootBuilder;

import static java.util.Objects.requireNonNull;

public final class QueryResolverBuilder {

    private QueryResolverBuilder() {

    }

    public static <X> QueryResolver<X> buildSimpleQueryResolver(Supplier<EntityManager> entityManager) {
        requireNonNull(entityManager, "entityManager");
        return parameters -> parameters.query().execute(
                entityManager.get(),
                EntityRootBuilder.from(parameters.entityType()));
    }
}
