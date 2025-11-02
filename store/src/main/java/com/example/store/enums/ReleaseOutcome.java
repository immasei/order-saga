package com.example.store.enums;

public enum ReleaseOutcome {
    SUCCESS,             // stock actually released // mark released ok
    NO_ACTION_REQUIRED,   // nothing was reserved // mark released ok
    REJECTED_NOT_ALLOWED // e.g., committed/shipped
}