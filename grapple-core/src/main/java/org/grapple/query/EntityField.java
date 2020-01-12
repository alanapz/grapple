package org.grapple.query;

import java.util.function.Function;
import javax.persistence.Tuple;
import org.grapple.core.MetadataAware;

public interface EntityField<X, T> extends MetadataAware {

    String getName();

    EntityResultType<T> getResultType();

    Function<Tuple, T> prepare(EntityContext<X> ctx, QueryBuilder queryBuilder);

}
