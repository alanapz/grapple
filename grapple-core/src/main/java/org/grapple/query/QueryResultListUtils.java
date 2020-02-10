package org.grapple.query;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.grapple.schema.NonScalarQueryResultException;

public final class QueryResultListUtils {

    private QueryResultListUtils() {

    }

    public static <X> QueryResultList<X> emptyResultList() {
        return new QueryResultList<X>() {

            @Override
            public int getTotalResults() {
                return 0;
            }

            @Override
            public int getRowsRetrieved() {
                return 0;
            }

            @Override
            public <U> List<U> map(Function<QueryResultRow<X>, U> resultMapper) {
                return emptyList();
            }

            @Override
            public <U> List<U> map(Supplier<U> objSupplier, BiConsumer<QueryResultRow<X>, U> consumer) {
                return emptyList();
            }

            @Override
            public Optional<QueryResultRow<X>> getUniqueResult() {
                return Optional.empty();
            }

            @Override
            public TabularResultList asTabular() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<QueryResultRow<X>> iterator() {
                return emptyIterator();
            }
        };
    }

    public static <X> QueryResultList<X> queryResultList(int totalResults, List<QueryResultRow<X>> sourceRows) {
        requireNonNull(sourceRows, "sourceRows");
        return new QueryResultList<X>() {

            private final List<QueryResultRow<X>> results = unmodifiableList(new ArrayList<>(sourceRows));

            @Override
            public int getTotalResults() {
                return totalResults;
            }

            @Override
            public int getRowsRetrieved() {
                return results.size();
            }

            @Override
            public <U> List<U> map(Function<QueryResultRow<X>, U> resultMapper) {
                requireNonNull(resultMapper, "resultMapper");
                return seq(results).map(resultMapper).toList();
            }

            @Override
            public <U> List<U> map(Supplier<U> objSupplier, BiConsumer<QueryResultRow<X>, U> consumer) {
                requireNonNull(objSupplier, "objSupplier");
                requireNonNull(consumer, "consumer");
                return seq(results).map(result -> {
                    final U instance = objSupplier.get();
                    consumer.accept(result, instance);
                    return instance;
                }).toList();            }

            @Override
            public Optional<QueryResultRow<X>> getUniqueResult() {
                if (results.isEmpty()) {
                    return Optional.empty();
                }
                if (results.size() != 1) {
                    throw new NonScalarQueryResultException("Query returned multiple rows");
                }
                return Optional.of(results.get(0));
            }

            @Override
            public TabularResultList asTabular() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator<QueryResultRow<X>> iterator() {
                return results.iterator();
            }
        };
    }
}
