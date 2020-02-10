package org.grapple.query;

import java.util.List;
import java.util.Optional;

public interface EntityResultList<X> extends Iterable<X> {

    int getTotalResults();

    int getEntitiesRetrieved();

    List<X> getResults();

    Optional<X> getUniqueResult();

}
