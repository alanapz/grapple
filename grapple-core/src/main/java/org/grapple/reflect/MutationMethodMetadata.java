package org.grapple.reflect;

import java.util.Map;
import org.grapple.utils.NoDuplicatesMap;

public interface MutationMethodMetadata {

    String getEndpointName();

    String getDescription();

    String getDeprecationReason();

//    EntityFilter<?> generate(DataFetchingEnvironment env, FetchSet<X> fetchSet, T args);

    public final Map<String, EntityQueryMetadata.EntityQueryParameterMetadata> parameters = new NoDuplicatesMap<>();
}

