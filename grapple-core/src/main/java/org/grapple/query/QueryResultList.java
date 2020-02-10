package org.grapple.query;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface QueryResultList<X> extends Iterable<QueryResultRow<X>> {

    int getTotalResults();

    int getRowsRetrieved();

    <U> List<U> map(Function<QueryResultRow<X>, U> resultMapper);

    <U> List<U> map(Supplier<U> objSupplier, BiConsumer<QueryResultRow<X>, U> consumer);

    Optional<QueryResultRow<X>> getUniqueResult();

    TabularResultList asTabular();
}
