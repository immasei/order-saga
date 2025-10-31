package com.example.store.exception;

import java.util.List;

public class InsufficientStockException extends RuntimeException {

    private final List<MissingItem> missing;

    public InsufficientStockException(String message, List<MissingItem> missing) {
        super(message);
        this.missing = missing;
    }

    public List<MissingItem> getMissing() {
        return missing;
    }

    public record MissingItem(String productCode, int required, int available) {}
}


