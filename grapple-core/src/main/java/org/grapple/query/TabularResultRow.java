package org.grapple.query;

import java.util.Map;
import org.grapple.utils.Chainable;

public interface TabularResultRow extends Chainable<TabularResultRow> {

    <X> boolean isExists(FetchSet<X> fetchSet);

    <X, T> T get(FetchSet<X> fetchSet, EntityField<X, T> field);

    <X, T> T getIfNotNull(FetchSet<X> fetchSet, EntityField<X, T> field, T valueIfNull);

    Map<String, Object> getValues();

}
