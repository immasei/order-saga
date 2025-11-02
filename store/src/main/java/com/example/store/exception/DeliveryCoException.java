package com.example.store.exception;

public class DeliveryCoException extends RuntimeException {
    private final int statusCode;

    public DeliveryCoException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}