package org.grapple.schema;

import static java.lang.String.format;

import java.lang.reflect.Executable;
import org.grapple.core.GrappleException;

public class DefinitionImportException extends GrappleException {

    public DefinitionImportException(String message) {
        super(message);
    }

    public DefinitionImportException(String message, Executable source) {
        super(format("%s (from: %s.%s)", message, source.getDeclaringClass().getName(), source.getName()));
    }

    public DefinitionImportException(String message, Throwable cause) {
        super(message, cause);
    }

}
