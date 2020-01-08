package org.grapple.query;

import java.util.function.Supplier;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import org.grapple.core.MetadataAware;

public interface EntityJoin<X, Y> extends MetadataAware {

    String getName();

    EntityResultType<Y> getResultType();

    Supplier<Join<?, Y>> join(EntityContext<X> ctx, QueryBuilder builder, Supplier<? extends From<?, X>> entity);
}
