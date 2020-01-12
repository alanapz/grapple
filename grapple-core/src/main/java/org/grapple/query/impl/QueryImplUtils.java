package org.grapple.query.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.grapple.query.FetchSet;

final class QueryImplUtils {

    private QueryImplUtils() {

    }

    static String resolveFullName(FetchSet<?> fetchSet) {
        requireNonNull(fetchSet, "fetchSet");
        return resolveFullName(fetchSet, null);
    }

    static String resolveFullName(FetchSet<?> fetchSet, String elementName) {
        requireNonNull(fetchSet, "fetchSet");
        final List<String> components = new ArrayList<>();
        while (fetchSet != null && fetchSet.getJoinedBy() != null) {
            components.add(fetchSet.getJoinedBy().getName());
            fetchSet = fetchSet.getFetchParent();
        }
        Collections.reverse(components);
        if (elementName != null) {
            components.add(elementName);
        }
        if (components.size() == 1) {
            return components.get(0);
        }
        return String.join(".", components);
    }

    // An empty set is one that recursively has no selections nor filters
    static boolean isEmptyFetchSet(FetchSet<?> fetchSet) {
        if (!fetchSet.getSelections().isEmpty()) {
            return false;
        }
        if (!fetchSet.getFilters().isEmpty()) {
            return false;
        }
        if (fetchSet.getFetchRoot().getOrderBy().stream().anyMatch(orderBy -> orderBy.getFetchSet() == fetchSet)) {
            return false;
        }
        if (fetchSet.getJoins().values().stream().anyMatch(joinedFetchSet -> !isEmptyFetchSet(joinedFetchSet))) {
            return false;
        }
        return true;
    }
}
