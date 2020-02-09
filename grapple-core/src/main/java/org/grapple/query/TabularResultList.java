package org.grapple.query;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TabularResultList extends Iterable<TabularResultRow> {

    int getTotalResultCount();

    boolean isEmpty();

    <U> List<U> map(Function<TabularResultRow, U> resultMapper);

    <U> List<U> map(Supplier<U> objSupplier, BiConsumer<TabularResultRow, U> consumer);

}