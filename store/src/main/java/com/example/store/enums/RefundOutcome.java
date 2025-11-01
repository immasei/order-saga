package com.example.store.enums;

public enum RefundOutcome {
    SUCCESS,     // mark refunded ok
    NO_ACTION_REQUIRED, // mark refunded ok
    REJECTED_NOT_ALLOWED,
    PROVIDER_ERROR // bank api time out // mark refunded ok
}
