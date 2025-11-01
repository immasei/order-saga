package com.example.store.enums;

public enum ReleaseOutcome {
    SUCCESS,           // stock actually released
    NOOP_ORPHAN,        // nothing was reserved
    REJECTED_NOT_ALLOWED // e.g., committed/shipped
}