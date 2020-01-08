//package org.grapple.schema;
//
//import org.grapple.query.QueryFilter;
//import org.grapple.utils.TypeToken;
//
//import java.util.Objects;
//import java.util.function.Function;
//
///* package */ final class EntityFilter<X> {
//
//    private final String name;
//
//    private final TypeToken<?> inputType;
//
//    private final Function<Object, EntityFilter<X>> filterCallback;
//
//    private EntityFilter(String name, TypeToken<?> inputType, Function<Object, EntityFilter<X>> filterCallback) {
//        this.name = Objects.requireNonNull(name, "name");
//        this.inputType = inputType;
//        this.filterCallback = Objects.requireNonNull(filterCallback, "filterCallback");
//    }
//}
