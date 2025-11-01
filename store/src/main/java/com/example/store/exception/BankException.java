package com.example.store.exception;

public class BankException extends RuntimeException {
    private final int statusCode;

    public BankException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}