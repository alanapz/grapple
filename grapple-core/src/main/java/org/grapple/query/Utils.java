package org.grapple.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class Utils {

    private Utils() {

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

    static String getFullName(FetchSet<?> fetchSet, String fieldName) {
        final List<String> components = new ArrayList<>();
        while (fetchSet != null && fetchSet.getJoinedBy() != null) {
            components.add(fetchSet.getJoinedBy().getName());
            fetchSet = fetchSet.getFetchParent();
        }
        Collections.reverse(components);
        if (fieldName != null) {
            components.add(fieldName);
        }
        return String.join(".", components);
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object o) {
        return (T) o;
    }
}
