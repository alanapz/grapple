package org.grapple.reflect;

import com.fasterxml.jackson.annotation.JsonTypeName;

public class TypeFactoryCallback {

    public String getInputTypeNameFor(Class<?> clazzm, JsonTypeName jsonTypeName) {
        return null;
    }

    public String getGenericTypeNameFor(Class<?> clazz) {
        return null;
    }


}
