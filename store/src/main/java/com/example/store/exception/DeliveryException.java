package com.example.store.exception;

public class DeliveryException extends RuntimeException {
    private final int statusCode;

    public DeliveryException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}