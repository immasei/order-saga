package com.example.bank.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Account has insufficient balance to perform the transaction.");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }

}