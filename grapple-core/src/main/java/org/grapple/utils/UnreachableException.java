package org.grapple.utils;

public class UnreachableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnreachableException() {
        super("Unreachable");
    }
}
