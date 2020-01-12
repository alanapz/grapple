package org.grapple.query;

import java.util.function.Supplier;
import javax.persistence.criteria.Join;

public interface JoinedEntityContext<X> extends EntityContext<X>, Supplier<Join<?, X>> {

}
