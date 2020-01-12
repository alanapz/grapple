package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.grapple.query.TabularResultList;
import org.grapple.query.TabularResultRow;

final class TabularResultListImpl implements TabularResultList {

    private final int totalResults;

    private final List<TabularResultRowImpl> results;

    TabularResultListImpl(int totalResults, List<TabularResultRowImpl> results) {
        requireNonNull(results, "results");
        this.totalResults = totalResults;
        this.results = results;
    }

    @Override
    public int getTotalResults() {
        return totalResults;
    }

    @Override
    public <U> List<U> map(Function<TabularResultRow, U> resultMapper) {
        requireNonNull(resultMapper, "resultMapper");
        return seq(results).map(resultMapper).toList();
    }

    @Override
    public <U> List<U> map(Supplier<U> objSupplier, BiConsumer<TabularResultRow, U> consumer) {
        requireNonNull(objSupplier, "objSupplier");
        requireNonNull(consumer, "consumer");
        return seq(results).map(result -> {
            final U instance = objSupplier.get();
            consumer.accept(result, instance);
            return instance;
        }).toList();
    }

    @Override
    public Iterator<TabularResultRow> iterator() {
        return seq(results).cast(TabularResultRow.class).iterator();
    }
}
