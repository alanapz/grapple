package org.grapple.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;

import static java.util.Objects.requireNonNull;

final class QueryWrapper {

    private final CriteriaQuery<?> query;

    private final QueryBuilder builder;

    private final List<Selection<?>> selections = new ArrayList<>();

    private final List<Predicate> predicates = new ArrayList<>();

    private final List<Order> orderBys = new ArrayList<>();

    QueryWrapper(CriteriaQuery<?> query, QueryBuilder builder) {
        this.query = requireNonNull(query, "query");
        this.builder = requireNonNull(builder, "builder");
    }

    CriteriaQuery<?> getQuery() {
        return query;
    }

    void select(Selection<?>... selection) {
        if (selection.length > 0) {
            this.selections.addAll(Arrays.asList(selection));
            query.multiselect(selections.toArray(new Selection[0]));
        }
    }

    void where(Predicate... predicate) {
        if (predicate.length > 0) {
            this.predicates.addAll(Arrays.asList(predicate));
            query.where(predicates.toArray(new Predicate[0]));
        }
    }

    void whereOr(Predicate... predicate) {
        if (predicate.length > 0) {
            predicates.add(builder.or(predicate));
            query.where(predicates.toArray(new Predicate[0]));
        }
    }

    QueryWrapper orderBy(Order orderBy) {
        requireNonNull(orderBy, "orderBy");
        orderBys.add(orderBy);
        query.orderBy(orderBys);
        return this;
    }
}
