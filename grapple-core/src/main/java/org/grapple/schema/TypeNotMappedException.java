package org.grapple.schema;

import org.grapple.core.GrappleException;

public class TypeNotMappedException extends GrappleException {

    public TypeNotMappedException(String message) {
        super(message);
    }

    public TypeNotMappedException(String message, Throwable cause) {
        super(message, cause);
    }

}
