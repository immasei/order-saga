package com.example.store.exception;

public class EmailException extends RuntimeException {
    private final int statusCode;

    public EmailException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}