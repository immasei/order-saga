package com.example.store.exception;

public class CancelledByUserException extends RuntimeException {

    public CancelledByUserException() {}
    public CancelledByUserException(String message) {
        super(message);
    }

}
