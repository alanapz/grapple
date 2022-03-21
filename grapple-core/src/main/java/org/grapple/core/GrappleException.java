package org.grapple.core;

import org.jetbrains.annotations.NotNull;

public abstract class GrappleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GrappleException(@NotNull String message) {
        super(message);
    }

    public GrappleException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
