package org.grapple.utils;

public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncheckedException() {
        super("Unreachable");
    }
}
