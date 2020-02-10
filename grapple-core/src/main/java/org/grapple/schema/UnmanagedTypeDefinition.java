package org.grapple.schema;

import java.util.Map;
import graphql.schema.DataFetcher;
import org.grapple.core.Chainable;
import org.grapple.core.ElementVisibility;
import org.grapple.core.Validatable;
import org.grapple.reflect.TypeLiteral;

public interface UnmanagedTypeDefinition<T> extends Chainable<UnmanagedTypeDefinition<T>>, Validatable {

    TypeLiteral<T> getType();

    ElementVisibility getVisibility();

    void setVisibility(ElementVisibility visibility);

    UnmanagedTypeBuilder getTypeBuilder();

    void setTypeBuilder(UnmanagedTypeBuilder typeBuilder);

    Map<String, DataFetcher<?>> getDataFetchers();

    void addDataFetcher(String fieldName, DataFetcher<?> dataFetcher);

}
