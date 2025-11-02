package com.example.store.exception;

public class NonRefundableException extends RuntimeException {

    public NonRefundableException(String message) {
        super(message);
    }

    public NonRefundableException(String message, Throwable cause) {
        super(message, cause);
    }
}

