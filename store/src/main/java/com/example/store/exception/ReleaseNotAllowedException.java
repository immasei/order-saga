package com.example.store.exception;

public class ReleaseNotAllowedException extends RuntimeException {

    public ReleaseNotAllowedException(String message) {
        super(message);
    }

    public ReleaseNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
